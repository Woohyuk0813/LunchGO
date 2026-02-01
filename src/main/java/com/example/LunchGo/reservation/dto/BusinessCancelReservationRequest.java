package com.example.LunchGo.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessCancelReservationRequest {
    private String reason;
    private String detail;
}
