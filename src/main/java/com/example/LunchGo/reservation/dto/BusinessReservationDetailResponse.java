package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BusinessReservationDetailResponse {
    private Long id;
    private String reservationCode;
    private String name;
    private String phone;
    private String date;       // YYYY-MM-DD
    private String time;       // HH:mm
    private Integer guests;
    private Integer amount;
    private String status;     // pending / confirmed / cancelled / refunded
    private String requestNote;
    private String paymentType; // prepaid / onsite
    private List<PreorderItem> preorderItems;

    @Getter
    @Builder
    public static class PreorderItem {
        private String name;
        private Integer qty;
        private Integer price;
    }
}