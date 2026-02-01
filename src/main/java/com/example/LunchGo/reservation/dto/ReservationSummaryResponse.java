package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationSummaryResponse {
    private RestaurantInfo restaurant;
    private BookingInfo booking;
    private PaymentInfo payment;
    private String requestNote;
    private Integer totalAmount;
    private Integer visitCount;
    private String paymentDeadlineAt;
    private String holdExpiresAt;
    private List<MenuItem> menuItems;

    @Getter
    @Builder
    public static class RestaurantInfo {
        private String name;
        private String address;
    }

    @Getter
    @Builder
    public static class BookingInfo {
        private String date;
        private String time;
        private Integer partySize;
        private String requestNote;
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private String type;
        private Integer amount;
    }

    @Getter
    @Builder
    public static class MenuItem {
        private String name;
        private Integer quantity;
        private Integer unitPrice;
        private Integer lineAmount;
    }
}
