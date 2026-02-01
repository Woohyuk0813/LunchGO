package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.annotation.DistributedLock;
import com.example.LunchGo.reservation.domain.*;
import com.example.LunchGo.reservation.dto.MenuSnapshot;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.reservation.exception.DuplicateReservationException;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationCreateRow;
import com.example.LunchGo.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    private final ReservationMapper reservationMapper;
    private final ReservationSlotService reservationSlotService;
    private final RedisUtil redisUtil; // 후처리 로직을 위해 RedisUtil 주입

    @Override
    @Deprecated
    public ReservationCreateResponse create(ReservationCreateRequest request) {
        // This method is deprecated. Use ReservationFacade instead.
        throw new UnsupportedOperationException("This method is deprecated. Please use ReservationFacade.createReservation().");
    }

    @Override
    @Transactional
    @DistributedLock(
            lockKey = "'reservation_process_lock:restaurant:' + #request.restaurantId + ':slot:' + #request.slotDate + ':' + #request.slotTime",
            userLockKey = "'reservation_lock:' + #request.userId + ':' + #request.restaurantId + ':' + #request.slotDate + ':' + #request.slotTime",
            userLockTime = 5000L
    )
    public ReservationCreateResponse createReservationLocked(
            ReservationCreateRequest request,
            Integer precalculatedPrepaySum,
            List<MenuSnapshot> menuSnapshots,
            Integer precalculatedDepositAmount) {

        // 지정한 날짜+시간대의 예약슬롯을 불러오는 서비스 로직(없으면 신규 생성)
        ReservationSlot slot = reservationSlotService.getValidatedSlot(
                request.getRestaurantId(),
                request.getSlotDate(),
                request.getSlotTime(),
                request.getPartySize()
        );

        // [중복 예약 사전 검증]
        // DB 유니크 제약조건(uk_user_slot_active) 위반 시 발생하는 SQL 에러 로그를 방지하고,
        // 불필요한 트랜잭션 롤백 비용을 절감하기 위해 애플리케이션 레벨에서 먼저 확인합니다.
        // DistributedLock(개인 락)은 5초 이내의 중복 요청만 막아주므로, 이 로직이 필수적입니다.
        if (reservationMapper.countActiveReservation(request.getUserId(), slot.getSlotId()) > 0) {
            throw new DuplicateReservationException("동일 시간대에 예약된 내역이 있습니다.");
        }

        Reservation reservation = new Reservation();
        reservation.setReservationCode("PENDING");
        reservation.setSlotId(slot.getSlotId());
        reservation.setUserId(request.getUserId());
        reservation.setPartySize(request.getPartySize());
        reservation.setReservationType(request.getReservationType());
        reservation.setStatus(ReservationStatus.TEMPORARY);
        reservation.setRequestMessage(trimToNull(request.getRequestMessage()));
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(7));
        reservation.setVisitStatus(VisitStatus.PENDING);

        if (ReservationType.RESERVATION_DEPOSIT.equals(request.getReservationType())) {
            reservation.setDepositAmount(precalculatedDepositAmount);
            reservation.setTotalAmount(precalculatedDepositAmount);
        } else if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
            // Facade에서 계산한 "DB 메뉴 합계" 기준으로 저장
            reservation.setPrepayAmount(precalculatedPrepaySum);
            reservation.setTotalAmount(precalculatedPrepaySum);
        }

        try {
            reservationMapper.insertReservation(reservation);
        } catch (DataIntegrityViolationException | PersistenceException e) {
            // 결제 대기 시간 만료 전, 약간의 텀을 두고 중복된 예약 요청이 발생했을 때의 중복 예약 방지
            throw new DuplicateReservationException("이미 결제 대기 중인 예약 요청입니다.");
        }

        // reservation_menu_items 저장 (예약 PK 생긴 다음에)
        if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType()) && menuSnapshots != null && !menuSnapshots.isEmpty()) {
            reservationMapper.insertReservationMenuItems(reservation.getReservationId(), menuSnapshots);
        }

        String code = generateReservationCode(LocalDate.now(), reservation.getReservationId());
        reservationMapper.updateReservationCode(reservation.getReservationId(), code);

        ReservationCreateRow row = reservationMapper.selectReservationCreateRow(reservation.getReservationId());
        if (row == null) {
            throw new IllegalStateException("created reservation not found");
        }

        // 후처리: 트랜잭션 커밋 후 Redis에 방문 상태 TTL 설정
        // 이 로직은 DB 트랜잭션 성공이 보장된 후에 실행되어야 데이터 정합성이 맞음.
        // @Transactional 범위 내에서 TransactionSynchronizationManager를 사용하면
        // 트랜잭션 커밋 직후에 콜백(afterCommit)을 실행할 수 있음.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // redis에 응답대기로 방문 확정 관련 상태 넣어놓기
                LocalDateTime slotDateTime = LocalDateTime.of(request.getSlotDate(), request.getSlotTime());
                long ttlMillis = Duration.between(LocalDateTime.now(), slotDateTime).toMillis();
                if (ttlMillis > 0) {
                    redisUtil.setDataExpire(String.valueOf(row.getReservationId()), VisitStatus.PENDING.name(), ttlMillis);
                }
            }
        });

        return new ReservationCreateResponse(
                row.getReservationId(),
                row.getReservationCode(),
                row.getSlotId(),
                row.getUserId(),
                row.getRestaurantId(),
                row.getSlotDate(),
                row.getSlotTime(),
                row.getPartySize(),
                row.getReservationType(),
                row.getStatus(),
                row.getRequestMessage()
        );
    }

    private static String generateReservationCode(LocalDate today, Long reservationId) {
        String date = today.format(CODE_DATE);
        String seq = String.format("%04d", reservationId);
        return "R" + date + "-" + seq;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public List<LocalTime> slotTimes(Long restaurantId, LocalDate slotDate) {
        return reservationMapper.selectSlotTimesByDate(restaurantId, slotDate);
    }
}
