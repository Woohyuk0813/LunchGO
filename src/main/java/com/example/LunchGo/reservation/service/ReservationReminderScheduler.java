package com.example.LunchGo.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ReservationReminderScheduler {

    private final ReservationReminderService reminderService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul") // 매 분
    public void run() {
        try {
            int sent = reminderService.sendDueReminders();
            if (sent > 0) {
                log.info("[ReservationReminderScheduler] sent={}", sent);
            }
        } catch (Exception e) {
            log.error("[ReservationReminderScheduler] failed", e);
        }
    }
}
