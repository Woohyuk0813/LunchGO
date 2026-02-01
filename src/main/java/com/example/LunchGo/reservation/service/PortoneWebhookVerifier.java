package com.example.LunchGo.reservation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class PortoneWebhookVerifier {
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${portone.webhook-secret}")
    private String webhookSecret;

    public void verify(String rawBody, WebRequest request) {
        // 1. 설정 확인
        if (!StringUtils.hasText(webhookSecret)) {
            throw new IllegalStateException("PortOne Webhook Secret이 설정되지 않았습니다.");
        }

        // 2. 헤더 확인
        String webhookId = request.getHeader("webhook-id");
        String webhookTimestamp = request.getHeader("webhook-timestamp");
        String webhookSignature = request.getHeader("webhook-signature");

        if (!StringUtils.hasText(webhookId) || !StringUtils.hasText(webhookTimestamp) || !StringUtils.hasText(webhookSignature)) {
            throw new IllegalArgumentException("웹훅 서명 검증에 필요한 헤더가 누락되었습니다.");
        }

        // 3. 서명 추출
        String receivedSignature = extractSignature(webhookSignature);

        // 4. 페이로드 구성 (id.timestamp.body)
        String payload = webhookId + "." + webhookTimestamp + "." + rawBody;

        // 5. 검증 수행
        try {
            String calculatedSignature = calculateHmacSha256(payload, webhookSecret);

            // 디버그 로그 (성공 시 제거 가능)
            System.out.println("=== Webhook Signature Check ===");
            System.out.println("Received:   " + receivedSignature);
            System.out.println("Calculated: " + calculatedSignature);

            if (!secureEquals(receivedSignature, calculatedSignature)) {
                throw new IllegalArgumentException("웹훅 서명이 일치하지 않습니다.");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("서명 검증 중 시스템 오류 발생", e);
        }
    }

    private String extractSignature(String headerValue) {
        String trimmed = headerValue.trim();
        if (trimmed.startsWith("v1,") || trimmed.startsWith("v1=")) {
            return trimmed.substring(3).trim();
        }
        return trimmed;
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        // [수정됨] whsec_ 접두어 처리 로직 추가
        String keyString = secret.trim();
        if (keyString.startsWith("whsec_")) {
            keyString = keyString.substring(6);
        }

        byte[] secretKeyBytes;
        try {
            secretKeyBytes = Base64.getDecoder().decode(keyString);
        } catch (IllegalArgumentException e) {
            // 만약 Base64가 아니라면 원본 그대로 사용 (구버전 호환용)
            secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, HMAC_SHA256);
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    private boolean secureEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}