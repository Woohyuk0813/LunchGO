package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessReservationItemResponse {
    private Long id;
    private String name;
    private String phone;
    private String datetime;
    private Integer guests;
    private Integer amount;
    private String status;
}
