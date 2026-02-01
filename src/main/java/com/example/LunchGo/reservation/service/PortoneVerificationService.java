package com.example.LunchGo.reservation.service;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PortoneVerificationService {
    private static final String STATUS_PAID = "PAID";

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${portone.api-base:https://api.portone.io}")
    private String apiBase;

    @Value("${portone.api-secret:}")
    private String apiSecret;

    @Value("${portone.http.connect-timeout-ms:5000}")
    private long connectTimeoutMs;

    @Value("${portone.http.read-timeout-ms:12000}")
    private long readTimeoutMs;

    @Value("${portone.http.max-attempts:2}")
    private int maxAttempts;

    @Value("${portone.http.retry-backoff-ms:300}")
    private long retryBackoffMs;

    public PortonePaymentInfo verifyPayment(String paymentId, Integer expectedAmount) {
        if (!StringUtils.hasText(apiSecret)) {
            throw new IllegalStateException("PortOne API Secret이 설정되지 않았습니다.");
        }

        String url = apiBase + "/payments/" + paymentId;
        RestTemplate restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
            .setReadTimeout(Duration.ofMillis(readTimeoutMs))
            .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = null;
        RestClientException lastError = null;
        int attempts = Math.max(1, maxAttempts);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                lastError = null;
                break;
            } catch (RestClientException ex) {
                lastError = ex;
                if (attempt == attempts) {
                    break;
                }
                if (retryBackoffMs > 0) {
                    try {
                        Thread.sleep(retryBackoffMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        if (response == null) {
            throw new IllegalStateException("PortOne 결제 조회 실패", lastError);
        }
        Map body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("PortOne 결제 조회 응답이 비어있습니다.");
        }

        Map data = (Map) body.get("data");
        if (data == null) {
            Object payment = body.get("payment");
            if (payment instanceof Map) {
                data = (Map) payment;
            } else {
                data = body;
            }
        }

        String status = stringValue(data.get("status"));
        Integer amount = extractAmount(data);

        if (!STATUS_PAID.equalsIgnoreCase(status)) {
            throw new IllegalStateException("결제가 완료 상태가 아닙니다.");
        }
        if (amount == null || !amount.equals(expectedAmount)) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        return PortonePaymentInfo.builder()
            .status(status)
            .amount(amount)
            .method(stringValue(data.get("method")))
            .cardType(stringValue(data.get("cardType")))
            .pgTid(stringValue(data.get("pgTid")))
            .build();
    }

    private String stringValue(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Integer extractAmount(Map data) {
        Object amountObj = data.get("amount");
        Integer amount = extractFromAmountObject(amountObj);
        if (amount != null) {
            return amount;
        }

        Object totalAmount = data.get("totalAmount");
        amount = intValue(totalAmount);
        if (amount != null) {
            return amount;
        }
        if (totalAmount instanceof Map) {
            amount = extractFromAmountObject(totalAmount);
            if (amount != null) {
                return amount;
            }
        }

        amount = intValue(data.get("paidAmount"));
        if (amount != null) {
            return amount;
        }
        return null;
    }

    private Integer extractFromAmountObject(Object amountObj) {
        if (amountObj instanceof Map) {
            Map amountMap = (Map) amountObj;
            Integer amount = intValue(amountMap.get("total"));
            if (amount != null) {
                return amount;
            }
            amount = intValue(amountMap.get("amount"));
            if (amount != null) {
                return amount;
            }
            amount = intValue(amountMap.get("value"));
            if (amount != null) {
                return amount;
            }
            amount = intValue(amountMap.get("totalAmount"));
            if (amount != null) {
                return amount;
            }
        }
        return intValue(amountObj);
    }

    private Integer intValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return null;
        }
        String stringValue = String.valueOf(value).trim();
        if (stringValue.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ignore) {
            try {
                double numeric = Double.parseDouble(stringValue);
                return (int) Math.round(numeric);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    @lombok.Builder
    @lombok.Getter
    public static class PortonePaymentInfo {
        private String status;
        private Integer amount;
        private String method;
        private String cardType;
        private String pgTid;
    }
}
