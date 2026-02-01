package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.dto.BusinessCancelReservationRequest;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationCancellation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.ReminderSendRow;
import com.example.LunchGo.reservation.repository.ReservationCancellationRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import com.example.LunchGo.restaurant.service.BusinessRestaurantService;
import com.example.LunchGo.sms.event.SystemSmsSendEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessReservationCancelService {
    private static final String CANCELLED_BY_OWNER = "OWNER";
    private static final String DEFAULT_REASON = "Owner cancellation";
    private static final int REASON_MAX_LENGTH = 255;

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final BusinessRestaurantService businessRestaurantService;
    private final ReservationCancellationRepository reservationCancellationRepository;
    private final ReservationRefundService reservationRefundService;
    private final ReservationMapper reservationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void cancel(Long reservationId, Long ownerId, BusinessCancelReservationRequest request) {
        if (reservationId == null) throw new IllegalArgumentException("reservationId is required");
        if (ownerId == null) throw new IllegalArgumentException("ownerId is required");

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(reservationId)));

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("slot not found: " + reservation.getSlotId()));

        Long restaurantId = businessRestaurantService.findRestaurantIdByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found for owner"));

        if (!restaurantId.equals(slot.getRestaurantId())) {
            throw new IllegalArgumentException("reservation does not belong to owner");
        }

        ensureCancellable(reservation.getStatus());
        if (reservationCancellationRepository.existsByReservationId(reservationId)) {
            throw new IllegalStateException("reservation already cancelled");
        }

        String reason = buildReason(request);

        ReservationCancellation cancellation = ReservationCancellation.builder()
                .reservationId(reservationId)
                .cancelledBy(CANCELLED_BY_OWNER)
                .cancelledById(ownerId)
                .reason(reason)
                .build();
        reservationCancellationRepository.save(cancellation);

        reservationRefundService.cancelByOwner(reservationId, reason);

        try {
            ReminderSendRow t = reservationMapper.selectCancelNoticeTarget(reservationId);
            if (t != null && t.getUserPhone() != null) {
                LocalDateTime slotDt = LocalDateTime.of(t.getSlotDate(), t.getSlotTime());
                String when = slotDt.format(DT);

                String text =
                        "[LunchGo] 예약이 취소되었습니다\n" +
                                t.getRestaurantName() + " (" + when + ")\n" +
                                "식당 사정으로 예약이 취소되었습니다.\n" +
                                "사유: " + reason;

                eventPublisher.publishEvent(new SystemSmsSendEvent(t.getUserPhone(), text));
            }
        } catch (Exception e) {
            log.warn("[BusinessReservationCancelService] owner-cancel SMS event publish failed. reservationId={}", reservationId, e);
        }
    }

    private void ensureCancellable(ReservationStatus status) {
        if (status == null) throw new IllegalArgumentException("reservation status is missing");
        if (ReservationStatus.TEMPORARY.equals(status)
                || ReservationStatus.CONFIRMED.equals(status)
                || ReservationStatus.PREPAID_CONFIRMED.equals(status)) {
            return;
        }
        throw new IllegalStateException("reservation cannot be cancelled: " + status);
    }

    private String buildReason(BusinessCancelReservationRequest request) {
        String base = request == null ? null : trimToNull(request.getReason());
        String detail = request == null ? null : trimToNull(request.getDetail());
        String reason = base != null ? base : DEFAULT_REASON;
        if (detail != null) reason = reason + " - " + detail;
        if (reason.length() > REASON_MAX_LENGTH) reason = reason.substring(0, REASON_MAX_LENGTH);
        return reason;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
