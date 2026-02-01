package com.example.LunchGo.reservation.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationPaymentExpiryScheduler {

    private final ReservationPaymentExpiryService expiryService;

    @Scheduled(fixedDelayString = "${reservation.payment-expiry.interval-ms:60000}")
    public void run() {
        try {
            int expired = expiryService.expireDuePayments(LocalDateTime.now());
            if (expired > 0) {
                log.info("[ReservationPaymentExpiryScheduler] expired={}", expired);
            }
        } catch (Exception e) {
            log.error("[ReservationPaymentExpiryScheduler] failed", e);
        }
    }
}
