package com.example.LunchGo.sms.event;

import com.example.LunchGo.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemSmsSendEventHandler {

    private final SmsService smsService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SystemSmsSendEvent event) {
        try {
            smsService.sendSystemSms(event.to(), event.text());
        } catch (Exception e) {
            log.warn("[SystemSmsSendEventHandler] SMS failed. to={}", event.to(), e);
        }
    }
}
