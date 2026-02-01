package com.example.LunchGo.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {
    private String paymentType;
    private Integer amount;
    private String method;
    private String idempotencyKey;
}
