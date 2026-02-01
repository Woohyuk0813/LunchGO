package com.example.LunchGo.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationSlot {
    private Long slotId;
    private Long restaurantId;
    private LocalDate slotDate;
    private LocalTime slotTime;
    private Integer maxCapacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
