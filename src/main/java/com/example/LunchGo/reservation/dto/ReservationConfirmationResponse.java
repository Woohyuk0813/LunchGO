package com.example.LunchGo.reservation.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationConfirmationResponse {
    private String reservationCode;
    private String status;
    private String cancelledBy;
    private RestaurantInfo restaurant;
    private BookingInfo booking;
    private PaymentInfo payment;

    // 주문 내역 (선주문/선결제일 때 내려줌)
    private List<MenuItem> menuItems;

    @Getter
    @Builder
    public static class RestaurantInfo {
        private String name;
        private String address;
        private String phone;
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
        private Integer amount;
        private String method;
        private String paidAt;
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
