package com.example.LunchGo.reservation.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.entity.Payment;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.repository.PaymentRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentExpiryService {
    private static final String PAYMENT_STATUS_FAILED = "FAILED";

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    // --- 새로 추가된 의존성 ---
    private final ReservationSlotRepository reservationSlotRepository; // ReservationSlot 조회를 위해 추가
    private final StringRedisTemplate stringRedisTemplate; // Redis Pipelining을 위해 추가

    @Transactional
    public int expireDuePayments(LocalDateTime now) {
        List<Reservation> targets = reservationRepository.findDueForPaymentExpiry(
            ReservationStatus.TEMPORARY,
            now
        );
        if (targets == null || targets.isEmpty()) {
            return 0;
        }

        // 1. 만료 처리할 예약들의 slotId를 모두 추출합니다. (N+1 문제 방지)
        List<Long> slotIds = targets.stream()
            .map(Reservation::getSlotId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // 2. 추출된 slotId를 이용해 모든 ReservationSlot 객체를 한 번에 조회하고, Map으로 변환합니다.
        Map<Long, ReservationSlot> slotMap = new HashMap<>();
        if (!slotIds.isEmpty()) {
            slotMap.putAll(
                reservationSlotRepository.findAllById(slotIds).stream()
                    .collect(Collectors.toMap(ReservationSlot::getSlotId, slot -> slot))
            );
        }

        // 3. Redis Pipelining으로 일괄 처리할 데이터를 수집하기 위한 Map을 초기화합니다.
        final Map<String, Long> seatsToRestore = new HashMap<>();

        Map<Long, Payment> latestPayments = loadLatestPayments(targets);
        int expired = 0;
        for (Reservation reservation : targets) {
            if (reservation == null || reservation.getReservationId() == null) {
                continue;
            }
            if (!ReservationStatus.TEMPORARY.equals(reservation.getStatus())) {
                continue;
            }
            reservation.setStatus(ReservationStatus.EXPIRED);
            log.info("결제 만료 처리: reservationId={}, ...", reservation.getReservationId());
            Payment payment = latestPayments.get(reservation.getReservationId());
            if (payment != null) {
                markPaymentFailed(payment, now);
            }

            // 4. Redis Pipelining을 위해 좌석 키와 복구할 인원수를 Map에 수집합니다.
            ReservationSlot slot = slotMap.get(reservation.getSlotId());
            Integer partySize = reservation.getPartySize();

            if (slot != null && partySize != null && partySize > 0) {
                String redisSeatKey = RedisUtil.generateSeatKey(
                    slot.getRestaurantId(),
                    slot.getSlotDate(),
                    slot.getSlotTime()
                );
                // 동일한 슬롯에 대한 만료 건이 여러 개일 경우를 대비해 예약만료로 풀린 좌석 수를 합산합니다.
                seatsToRestore.merge(redisSeatKey, partySize.longValue(), Long::sum);
            }

            expired++;
        }

        // 5. for문이 끝난 후, 수집된 정보가 있을 경우에만 DB 커밋 후 실행될 작업을 단 한 번 등록합니다.
        if (!seatsToRestore.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("결제 만료로 인한 좌석 일괄 복구 시작. 대상 슬롯 수: {}", seatsToRestore.size());
                    // 6. Redis Pipelining을 사용하여 모든 INCRBY 명령을 한 번의 네트워크 요청으로 전송합니다.
                    stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                        seatsToRestore.forEach((key, value) -> {
                            connection.stringCommands().incrBy(
                                stringRedisTemplate.getStringSerializer().serialize(key),
                                value
                            );
                        });
                        return null; // Pipelining에서는 보통 null을 반환합니다.
                    });
                    log.info("좌석 일괄 복구 완료.");
                }
            });
        }

        return expired;
    }

    private Map<Long, Payment> loadLatestPayments(List<Reservation> reservations) {
        Map<Long, Payment> latestMap = new HashMap<>();
        List<Long> reservationIds = reservations.stream()
            .filter(Objects::nonNull)
            .map(Reservation::getReservationId)
            .filter(Objects::nonNull)
            .toList();
        if (reservationIds.isEmpty()) {
            return latestMap;
        }
        List<Payment> payments = paymentRepository.findByReservationIdInOrderByReservationIdAscCreatedAtDesc(
            reservationIds
        );
        for (Payment payment : payments) {
            if (payment == null || payment.getReservationId() == null) {
                continue;
            }
            latestMap.putIfAbsent(payment.getReservationId(), payment);
        }
        return latestMap;
    }

    private void markPaymentFailed(Payment payment, LocalDateTime now) {
        if (payment == null) {
            return;
        }
        payment.setStatus(PAYMENT_STATUS_FAILED);
        payment.setFailedAt(now);
    }
}