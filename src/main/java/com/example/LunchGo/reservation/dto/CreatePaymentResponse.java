package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatePaymentResponse {
    private Long paymentId;
    private String merchantUid;
    private Integer amount;
    private String pgProvider;
    private String currency;
}
