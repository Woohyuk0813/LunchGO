package com.example.LunchGo.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelReservationRequest {
    private String reason;
    private String detail;
    private RefundInfo refund;

    @Getter
    @Setter
    public static class RefundInfo {
        private Boolean requested;
        private Integer amount;
        private String paymentKey;
        private String transactionId;
        private String pgProvider;
    }
}
