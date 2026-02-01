package com.example.LunchGo.reservation.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PortoneCancelService {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${portone.api-base:https://api.portone.io}")
    private String apiBase;

    @Value("${portone.api-secret:}")
    private String apiSecret;

    @Value("${portone.http.connect-timeout-ms:5000}")
    private long connectTimeoutMs;

    @Value("${portone.http.read-timeout-ms:12000}")
    private long readTimeoutMs;

    // paymentId = 이 프로젝트에선 merchantUid
    public void cancelPayment(String paymentId, String reason, int amount) {
        if (!StringUtils.hasText(apiSecret)) {
            throw new IllegalStateException("PortOne API Secret이 설정되지 않았습니다.");
        }
        if (!StringUtils.hasText(paymentId)) {
            throw new IllegalArgumentException("paymentId is required");
        }
        if (!StringUtils.hasText(reason)) {
            reason = "예약 취소";
        }

        String url = apiBase + "/payments/" + paymentId + "/cancel";

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("reason", reason);
        body.put("amount", amount); // 정책상 부분환불 가능하니 항상 명시

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class
            );
            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("PortOne 결제 취소 실패: " + res.getStatusCode());
            }
        } catch (RestClientException ex) {
            throw new IllegalStateException("PortOne 결제 취소 실패", ex);
        }
    }
}
