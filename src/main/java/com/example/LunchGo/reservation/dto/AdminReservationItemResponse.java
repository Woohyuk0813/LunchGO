package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReservationItemResponse {
    private String id;
    private String restaurantName;
    private String customerName;
    private String reservationDateTime;
    private Integer partySize;
    private String type;
    private String status;
}
