package com.example.LunchGo.reservation.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OwnerReservationCancelledEventHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OwnerReservationCancelledEvent event) {
        try {

        } catch (Exception e) {
            log.warn("[OwnerReservationCancelledEventHandler] failed. reservationId={}",
                    event.reservationId(), e);
        }
    }
}
