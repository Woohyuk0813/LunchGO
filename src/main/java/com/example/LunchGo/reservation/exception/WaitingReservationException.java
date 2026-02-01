package com.example.LunchGo.reservation.exception;

import lombok.Getter;

@Getter
public class WaitingReservationException extends RuntimeException {
    
    // 현재 대기 중인 인원 수 (본인 포함)
    private final long waitingCount;

    public WaitingReservationException(String message) {
        super(message);
        this.waitingCount = 0;
    }

    public WaitingReservationException(String message, long waitingCount) {
        super(message);
        this.waitingCount = waitingCount;
    }
}
