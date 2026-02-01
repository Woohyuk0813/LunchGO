package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.dto.PortoneCompleteRequest;
import com.example.LunchGo.reservation.dto.PortoneFailRequest;
import com.example.LunchGo.reservation.dto.PortoneRequestedRequest;
import com.example.LunchGo.reservation.dto.PortoneWebhookRequest;
import com.example.LunchGo.reservation.service.PortoneWebhookVerifier;
import com.example.LunchGo.reservation.service.ReservationPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class PaymentController {
    private final ReservationPaymentService reservationPaymentService;
    private final PortoneWebhookVerifier portoneWebhookVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping("/payments/portone/complete")
    public ResponseEntity<?> completePayment(@RequestBody PortoneCompleteRequest request) {
        reservationPaymentService.completePayment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payments/portone/fail")
    public ResponseEntity<?> failPayment(@RequestBody PortoneFailRequest request) {
        reservationPaymentService.failPayment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payments/portone/requested")
    public ResponseEntity<?> markPaymentRequested(@RequestBody PortoneRequestedRequest request) {
        try {
            reservationPaymentService.markPaymentRequested(request.getMerchantUid());
        } catch (Exception e) {
            log.warn("결제 요청 시작 기록 실패", e);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payments/portone/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String rawBody, WebRequest webRequest) {
        portoneWebhookVerifier.verify(rawBody, webRequest);
        try {
            PortoneWebhookRequest request = objectMapper.readValue(rawBody, PortoneWebhookRequest.class);
            String eventType = request.getType();
            String paymentId = request.getData() != null ? request.getData().getPaymentId() : null;
            log.info("PortOne webhook received: type={}, paymentId={}", eventType, paymentId);

            if ("Transaction.Paid".equalsIgnoreCase(eventType)) {
                reservationPaymentService.handleWebhookPaid(paymentId);
            } else if ("Transaction.Failed".equalsIgnoreCase(eventType)) {
                reservationPaymentService.handleWebhookFailed(paymentId);
            } else if ("Transaction.Cancelled".equalsIgnoreCase(eventType)) {
                reservationPaymentService.handleWebhookCancelled(paymentId);
            }
        } catch (Exception e) {
            log.warn("웹훅 처리 실패", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().build();
    }
}
