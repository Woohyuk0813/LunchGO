package com.example.LunchGo.reservation.event;

public record OwnerReservationCancelledEvent(Long reservationId, String reason) {}
