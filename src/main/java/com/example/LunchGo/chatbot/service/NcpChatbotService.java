package com.example.LunchGo.chatbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NcpChatbotService {

    private static final String SIGNATURE_HEADER = "X-NCP-CHATBOT_SIGNATURE";
    private static final String VERSION = "v2";
    private static final String EVENT_SEND = "send";
    private static final String EVENT_OPEN = "open";

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ncp.chatbot.api-url}")
    private String apiUrl;

    @Value("${ncp.chatbot.secret-key}")
    private String secretKey;

    public String sendMessage(String message, String userId, String userIp) {
        if (message == null || message.isBlank()) {
            return "";
        }
        String resolvedUserId = resolveUserId(userId);
        String requestBody = buildRequestBody(EVENT_SEND, message, resolvedUserId, userIp);
        return executeRequest(requestBody);
    }

    public String open(String message, String userId, String userIp) {
        String resolvedUserId = resolveUserId(userId);
        String resolvedMessage = (message == null || message.isBlank()) ? "open" : message;
        String requestBody = buildRequestBody(EVENT_OPEN, resolvedMessage, resolvedUserId, userIp);
        return executeRequest(requestBody);
    }

    private String resolveUserId(String userId) {
        return (userId == null || userId.isBlank())
                ? UUID.randomUUID().toString()
                : userId;
    }

    private String executeRequest(String requestBody) {
        String signature = makeSignature(requestBody, secretKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(SIGNATURE_HEADER, signature);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return response.getBody() == null ? "" : response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "NCP chatbot request failed", ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "NCP chatbot request failed", ex);
        }
    }

    private String buildRequestBody(String event, String message, String userId, String userIp) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("version", VERSION);
        payload.put("userId", userId);
        if (userIp != null && !userIp.isBlank()) {
            payload.put("userIp", userIp);
        }
        payload.put("timestamp", System.currentTimeMillis());

        Map<String, Object> bubble = new LinkedHashMap<>();
        bubble.put("type", "text");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("description", message);
        bubble.put("data", data);

        payload.put("bubbles", List.of(bubble));
        payload.put("event", event);

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Chatbot payload build failed", ex);
        }
    }

    private String makeSignature(String message, String secretKey) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Chatbot signature build failed", ex);
        }
    }
}
