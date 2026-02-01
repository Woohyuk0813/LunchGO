package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.ReservationHistoryItem;
import com.example.LunchGo.reservation.mapper.ReservationHistoryMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationHistoryRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationHistoryServiceImpl implements ReservationHistoryService {

    private final ReservationHistoryMapper reservationHistoryMapper;

    @Override
    public List<ReservationHistoryItem> getHistory(Long userId, String type) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        String normalized = type == null ? "past" : type.trim().toLowerCase();
        List<String> statuses = null;
        String orderDirection = "DESC";
        if ("past".equals(normalized)) {
            statuses = Arrays.asList("COMPLETED", "REFUND_PENDING", "REFUNDED", "CANCELLED");
            orderDirection = "DESC";
        } else if ("upcoming".equals(normalized)) {
            statuses = Arrays.asList("TEMPORARY", "CONFIRMED", "PREPAID_CONFIRMED", "CANCELLED");
            orderDirection = "ASC";
        } else if ("all".equals(normalized)) {
            statuses = null;
            orderDirection = "DESC";
        } else {
            throw new IllegalArgumentException("type must be past, upcoming, or all");
        }

        List<ReservationHistoryRow> rows = reservationHistoryMapper.selectReservationHistory(
            userId,
            statuses,
            orderDirection
        );
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReservationHistoryItem> items = new ArrayList<>();
        for (ReservationHistoryRow row : rows) {
            ReservationHistoryItem.RestaurantInfo restaurant = new ReservationHistoryItem.RestaurantInfo(
                row.getRestaurantId(),
                row.getRestaurantName(),
                row.getRestaurantAddress()
            );
            ReservationHistoryItem.BookingInfo booking = new ReservationHistoryItem.BookingInfo(
                row.getSlotDate(),
                row.getSlotTime(),
                row.getPartySize()
            );

            Integer amount = resolvePaymentAmount(row);
            ReservationHistoryItem.PaymentInfo payment = amount == null
                ? null
                : new ReservationHistoryItem.PaymentInfo(amount);

            ReservationHistoryItem.ReviewInfo review = null;
            if (row.getReviewId() != null) {
                List<String> tags = parseTags(row.getReviewTags());
                review = new ReservationHistoryItem.ReviewInfo(
                    row.getReviewId(),
                    row.getReviewRating(),
                    row.getReviewContent(),
                    tags,
                    row.getReviewCreatedAt()
                );
            }

            items.add(new ReservationHistoryItem(
                row.getReservationId(),
                row.getReservationCode(),
                restaurant,
                booking,
                row.getVisitCount(),
                row.getDaysSinceLastVisit(),
                payment,
                row.getReservationStatus(),
                row.getCancelledBy(),
                row.getCancelledReason(),
                row.getCancelledAt(),
                review
            ));
        }
        return items;
    }

    private Integer resolvePaymentAmount(ReservationHistoryRow row) {
        if (row.getReceiptAmount() != null) {
            return row.getReceiptAmount();
        }
        if (row.getPaidAmount() != null) {
            return row.getPaidAmount();
        }
        return row.getTotalAmount();
    }

    private List<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(rawTags.split("\\|\\|"));
    }
}
