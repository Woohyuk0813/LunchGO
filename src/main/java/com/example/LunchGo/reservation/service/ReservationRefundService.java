package com.example.LunchGo.reservation.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.entity.Payment;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.repository.PaymentRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ReservationRefundService {

    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";

    private static final String PAYMENT_TYPE_DEPOSIT = "DEPOSIT";
    private static final String PAYMENT_TYPE_PREPAID = "PREPAID_FOOD";

    private static final String CARD_TYPE_CORPORATE = "CORPORATE";

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentRepository paymentRepository;
    private final PortoneCancelService portoneCancelService;
    private final RedisUtil redisUtil; // Redis 좌석 카운터 관리를 위해 추가

    //회원(사용자) 취소
    @Transactional
    public int cancelWithRefundPolicy(Long reservationId, String reason) {
        return cancelInternal(reservationId, reason, CancelActor.MEMBER);
    }

    //점주 강제 취소: card_type 무관 100% 환불
    @Transactional
    public int cancelByOwner(Long reservationId, String reason) {
        return cancelInternal(reservationId, reason, CancelActor.OWNER);
    }

    private int cancelInternal(Long reservationId, String reason, CancelActor actor) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.EXPIRED
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("취소할 수 없는 예약 상태입니다: " + reservation.getStatus());
        }

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("예약 슬롯 정보를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationAt = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
        LocalDate reservationDate = slot.getSlotDate();

        int partySize = reservation.getPartySize() != null ? reservation.getPartySize() : 0;

        Optional<Payment> prepaidOpt = paymentRepository
                .findByReservationIdAndPaymentType(reservationId, PAYMENT_TYPE_PREPAID)
                .filter(p -> PAYMENT_STATUS_PAID.equals(p.getStatus()));

        Optional<Payment> depositOpt = paymentRepository
                .findByReservationIdAndPaymentType(reservationId, PAYMENT_TYPE_DEPOSIT)
                .filter(p -> PAYMENT_STATUS_PAID.equals(p.getStatus()));

        // 결제 자체가 없으면(미결제/현장결제 등) -> 그냥 취소
        if (prepaidOpt.isEmpty() && depositOpt.isEmpty()) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            return 0;
        }

        String finalReason = (reason == null || reason.isBlank()) ? "예약 취소" : reason;

        int refundedTotal = 0;
        reservation.setStatus(ReservationStatus.REFUND_PENDING);

        // 1) 선결제(음식값 전액 결제) 환불: 메뉴잔액 100% + 예약금가정액*환불율
        if (prepaidOpt.isPresent()) {
            Payment prepaid = prepaidOpt.get();

            int refundAmount = computeRefundAmountForPrepaid(
                    prepaid,
                    now,
                    reservationAt,
                    reservationDate,
                    partySize,
                    reservation.getStatus(),
                    actor
            );

            if (refundAmount > 0) {
                portoneCancelService.cancelPayment(prepaid.getMerchantUid(), finalReason, refundAmount);
                prepaid.setStatus(PAYMENT_STATUS_CANCELLED);
                prepaid.setCancelledAt(LocalDateTime.now());
                refundedTotal += refundAmount;
            }
            // refundAmount==0이면 정책상 환불 호출/상태변경 안 함(결제금 몰수 성격)
        }

        // 2) 예약금 결제 환불: 예약금 전액 * 환불율(예외 반영)
        if (depositOpt.isPresent()) {
            Payment deposit = depositOpt.get();

            int refundAmount = computeRefundAmountForDeposit(
                    deposit,
                    now,
                    reservationAt,
                    reservationDate,
                    partySize,
                    reservation.getStatus(),
                    actor
            );

            if (refundAmount > 0) {
                portoneCancelService.cancelPayment(deposit.getMerchantUid(), finalReason, refundAmount);
                deposit.setStatus(PAYMENT_STATUS_CANCELLED);
                deposit.setCancelledAt(LocalDateTime.now());
                refundedTotal += refundAmount;
            }
        }

        // 정책: 예약 상태는 취소 상태로 업데이트
        // (프로젝트가 좌석잔여를 파생 계산하는 구조라 CANCELLED로 바꾸는 순간 자동 복구됨)
        reservation.setStatus(
                refundedTotal > 0 ? ReservationStatus.REFUNDED : ReservationStatus.CANCELLED
        );

        // --- Redis 좌석 카운터 업데이트 (트랜잭션 커밋 후) ---
        // DB 트랜잭션이 성공적으로 커밋된 후에만 Redis 좌석 카운터를 증가시켜 일관성을 유지합니다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 예약 슬롯 정보 (restaurantId, slotDate, slotTime) 및 partySize를 가져와 Redis 키 생성
                String redisSeatKey = RedisUtil.generateSeatKey(slot.getRestaurantId(), slot.getSlotDate(), slot.getSlotTime());
                int partySize = reservation.getPartySize() != null ? reservation.getPartySize() : 0;

                // Redis 좌석 카운터에 partySize만큼 좌석 반환 (증가)
                // 만약 Redis 키가 중간에 만료되었더라도, increment는 0에서부터 시작하여 값을 추가하므로 문제 없음
                redisUtil.increment(redisSeatKey, partySize);
            }
        });

        return refundedTotal;
    }

    /**
     * 예약금만 결제한 건
     * - 기본: 예약금 * 환불율
     * - 예외1) 회원취소 + 법인카드: 위약금 면제 => 100%
     * - 예외2) 점주강제취소: 100%
     */
    private int computeRefundAmountForDeposit(
            Payment payment,
            LocalDateTime now,
            LocalDateTime reservationAt,
            LocalDate reservationDate,
            int partySize,
            ReservationStatus currentStatus,
            CancelActor actor
    ) {
        int paidAmount = payment.getAmount() != null ? payment.getAmount() : 0;
        if (paidAmount <= 0) return 0;

        // 점주 강제 취소: 무조건 100%
        if (actor == CancelActor.OWNER) return paidAmount;

        // 회원 취소 + 법인카드: 위약금 면제(전액 환불)
        if (isCorporateCard(payment)) return paidAmount;

        int percent = computeTimeRefundPercent(now, reservationAt, reservationDate, partySize, currentStatus);
        return (int) Math.floor(paidAmount * (percent / 100.0));
    }

    /**
     * 선결제(음식값 전액 결제) 건
     * - 메뉴잔액: 무조건 100% 환불
     * - 예약금 가정액: 취소시점 환불율 적용
     * - 예외1) 회원취소 + 법인카드: 위약금 면제 => 전액 환불
     * - 예외2) 점주강제취소: 전액 환불
     */
    private int computeRefundAmountForPrepaid(
            Payment payment,
            LocalDateTime now,
            LocalDateTime reservationAt,
            LocalDate reservationDate,
            int partySize,
            ReservationStatus currentStatus,
            CancelActor actor
    ) {
        int total = payment.getAmount() != null ? payment.getAmount() : 0;
        if (total <= 0) return 0;

        // 점주 강제 취소: 무조건 100%
        if (actor == CancelActor.OWNER) return total;

        // 회원 취소 + 법인카드: 위약금 면제(전액 환불)
        if (isCorporateCard(payment)) return total;

        // 1) 인당 예약금 기준 금액
        int perPersonDeposit = (partySize >= 7) ? 10_000 : 5_000;

        // 2) 예약금 가정액(클램프: 총액보다 커질 수 없음)
        int assumedDeposit = perPersonDeposit * Math.max(partySize, 0);
        assumedDeposit = Math.min(assumedDeposit, total);

        // 3) 메뉴금액 잔액(항상 100%)
        int menuRemainder = total - assumedDeposit;
        if (menuRemainder < 0) menuRemainder = 0;

        // 4) 예약금 가정액에만 환불율 적용
        int percent = computeTimeRefundPercent(now, reservationAt, reservationDate, partySize, currentStatus);
        int depositRefund = (int) Math.floor(assumedDeposit * (percent / 100.0));

        // 5) 최종 환불 = 메뉴잔액 100% + 예약금가정액*환불율
        return menuRemainder + depositRefund;
    }

    /**
     * 취소 시점 기반 환불율
     * 1) 전일 취소: 100%
     * 2) 당일 2시간 전까지: 50%
     * 3) 2시간 이내: 20% (8인 이상 10%)
     * 4) 노쇼: 0%
     */
    private int computeTimeRefundPercent(
            LocalDateTime now,
            LocalDateTime reservationAt,
            LocalDate reservationDate,
            int partySize,
            ReservationStatus currentStatus
    ) {
        // 노쇼: 0%
        if (currentStatus == ReservationStatus.NO_SHOW) return 0;

        // 예약시간 이후 취소 시도: 노쇼 취급(0%)
        if (!now.isBefore(reservationAt)) return 0;

        // 예약 전일(캘린더 기준): 100%
        if (now.toLocalDate().isBefore(reservationDate)) return 100;

        // 당일 2시간 전까지(2시간 전 포함): 50%
        if (!now.isAfter(reservationAt.minusHours(2))) return 50;

        // 예약시간 2시간 이내: 20% (8인 이상: 10%)
        return partySize >= 8 ? 10 : 20;
    }

    private boolean isCorporateCard(Payment payment) {
        if (payment == null) return false;
        String cardType = payment.getCardType();
        return cardType != null && CARD_TYPE_CORPORATE.equalsIgnoreCase(cardType.trim());
    }

    private enum CancelActor {
        MEMBER,
        OWNER
    }
}
