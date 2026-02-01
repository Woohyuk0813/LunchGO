package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.dto.CreatePaymentRequest;
import com.example.LunchGo.reservation.dto.CreatePaymentResponse;
import com.example.LunchGo.reservation.dto.ReservationConfirmationResponse;
import com.example.LunchGo.reservation.dto.ReservationPaymentStatusResponse;
import com.example.LunchGo.reservation.dto.ReservationSummaryResponse;
import com.example.LunchGo.reservation.service.ReservationPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReservationPaymentController {
    private final ReservationPaymentService reservationPaymentService;

    @PostMapping("/reservations/{reservationId}/payments")
    public ResponseEntity<CreatePaymentResponse> createPayment(
        @PathVariable Long reservationId,
        @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentResponse response = reservationPaymentService.createPayment(reservationId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/reservations/{reservationId}/payments/expire")
    public ResponseEntity<?> expirePayment(@PathVariable Long reservationId) {
        reservationPaymentService.expirePayment(reservationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/reservations/{reservationId}/confirmation")
    public ResponseEntity<ReservationConfirmationResponse> getConfirmation(@PathVariable Long reservationId) {
        ReservationConfirmationResponse response = reservationPaymentService.getConfirmation(reservationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reservations/{reservationId}/confirmation/status")
    public ResponseEntity<ReservationPaymentStatusResponse> getConfirmationStatus(@PathVariable Long reservationId) {
        ReservationPaymentStatusResponse response = reservationPaymentService.getConfirmationStatus(reservationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/reservations/{reservationId}/summary")
    public ResponseEntity<ReservationSummaryResponse> getSummary(@PathVariable Long reservationId) {
        ReservationSummaryResponse response = reservationPaymentService.getSummary(reservationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
