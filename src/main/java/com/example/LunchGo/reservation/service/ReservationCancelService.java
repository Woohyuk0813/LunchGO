package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.dto.CancelReservationRequest;
import com.example.LunchGo.reservation.entity.Payment;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.repository.PaymentRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCancelService {

    private static final String PAYMENT_STATUS_PAID = "PAID";

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentRepository paymentRepository;
    private final PortoneCancelService portoneCancelService;

    @Transactional
    public void cancel(Long reservationId, CancelReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

        // 이미 끝난/취소된 건 방어
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.REFUNDED
                || reservation.getStatus() == ReservationStatus.NO_SHOW
                || reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new IllegalStateException("취소할 수 없는 예약 상태입니다: " + reservation.getStatus());
        }

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("예약 슬롯 정보를 찾을 수 없습니다."));

        LocalDate slotDate = slot.getSlotDate();
        LocalTime slotTime = slot.getSlotTime();
        LocalDateTime reservationDateTime = LocalDateTime.of(slotDate, slotTime);
        LocalDateTime now = LocalDateTime.now();

        // 결제된 예약(예약금/선결제)만 환불 대상
        Payment paidPayment = paymentRepository
                .findTopByReservationIdAndStatusOrderByApprovedAtDesc(reservationId, PAYMENT_STATUS_PAID)
                .orElse(null);

        // TEMPORARY(미결제)면 그냥 취소 처리만
        if (paidPayment == null) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setHoldExpiresAt(null);
            reservation.setPaymentDeadlineAt(null);
            // 좌석 복원은 이 프로젝트 구조상 status 집계에서 빠지면서 자동 반영
            return;
        }

        int paidAmount = paidPayment.getAmount() != null ? paidPayment.getAmount() : 0;
        int partySize = reservation.getPartySize() != null ? reservation.getPartySize() : 0;

        int refundPercent = computeRefundPercent(now, reservationDateTime, slotDate, partySize, reservation.getStatus());
        int refundAmount = (int) Math.floor(paidAmount * (refundPercent / 100.0));

        // 노쇼(0%)면 결제취소(환불) 호출 안 함. 예약만 취소 처리.
        if (refundAmount <= 0) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setHoldExpiresAt(null);
            reservation.setPaymentDeadlineAt(null);
            return;
        }

        // 환불 진행 -> 성공 시 환불완료 상태
        reservation.setStatus(ReservationStatus.REFUND_PENDING);

        String reason = (request != null && request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason()
                : "예약 취소";

        // paymentId는 이 프로젝트에서 merchantUid를 결제조회(paymentId)로 이미 사용 중
        // (PortoneVerificationService가 /payments/{paymentId}에 merchantUid를 넣어서 조회함)
        portoneCancelService.cancelPayment(paidPayment.getMerchantUid(), reason, refundAmount);

        // 결제 취소 완료로 기록 (프로젝트에 환불 테이블 없으니 최소로 payment status/시간만 남김)
        paidPayment.setStatus("CANCELLED");
        paidPayment.setCancelledAt(LocalDateTime.now());

        reservation.setStatus(ReservationStatus.REFUNDED);
        reservation.setHoldExpiresAt(null);
        reservation.setPaymentDeadlineAt(null);

        // 좌석 복원: status가 집계 대상에서 빠지므로 자동 복원
    }

    private int computeRefundPercent(
            LocalDateTime now,
            LocalDateTime reservationDateTime,
            LocalDate reservationDate,
            int partySize,
            ReservationStatus currentStatus
    ) {
        // 이미 NO_SHOW로 찍힌 경우
        if (currentStatus == ReservationStatus.NO_SHOW) {
            return 0;
        }

        // 예약시간 이후 취소는 노쇼 취급(0%)
        if (!now.isBefore(reservationDateTime)) {
            return 0;
        }

        // 예약 전일(= 예약 날짜보다 이전 날짜) 취소: 100%
        if (now.toLocalDate().isBefore(reservationDate)) {
            return 100;
        }

        // 예약 당일: 예약시간 2시간 전까지 50%
        if (!now.isAfter(reservationDateTime.minusHours(2))) {
            return 50;
        }

        // 예약시간 2시간 이내 취소: 20% (8인 이상은 10%)
        return partySize >= 8 ? 10 : 20;
    }
}
