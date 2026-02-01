package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationPaymentStatusResponse {
    private boolean paid;
    private String paidAt;
}
