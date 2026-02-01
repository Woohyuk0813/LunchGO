package com.example.LunchGo.reservation.exception;

public class SlotCapacityExceededException extends RuntimeException {
    public SlotCapacityExceededException(String message) {
        super(message);
    }
}
