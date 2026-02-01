package com.example.LunchGo.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationHistoryItem {

    private Long id;
    private String confirmationNumber;
    private RestaurantInfo restaurant;
    private BookingInfo booking;
    private Integer visitCount;
    private Integer daysSinceLastVisit;
    private PaymentInfo payment;
    private String reservationStatus;
    private String cancelledBy;
    private String cancelledReason;
    private LocalDateTime cancelledAt;
    private ReviewInfo review;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantInfo {
        private Long id;
        private String name;
        private String address;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingInfo {
        private LocalDate date;
        private LocalTime time;
        private Integer partySize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private Integer amount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInfo {
        private Long id;
        private Integer rating;
        private String content;
        private List<String> tags;
        private LocalDate createdAt;
    }
}
