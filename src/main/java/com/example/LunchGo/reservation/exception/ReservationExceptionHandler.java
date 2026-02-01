package com.example.LunchGo.reservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.LunchGo.reservation")
public class ReservationExceptionHandler {

    @ExceptionHandler({
            DuplicateReservationException.class,
            SlotCapacityExceededException.class,
            WaitingReservationException.class
    })
    public ResponseEntity<Map<String, Object>> handleReservationExceptions(RuntimeException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());

        if (e instanceof WaitingReservationException) {
            response.put("waitingCount", ((WaitingReservationException) e).getWaitingCount());
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
