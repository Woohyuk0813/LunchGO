package com.example.LunchGo.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortoneWebhookRequest {
    private String type;
    private String timestamp;
    private Data data;

    @Getter
    @Setter
    public static class Data {
        private String storeId;
        private String paymentId;
        private String transactionId;
        private String cancellationId;
    }
}
