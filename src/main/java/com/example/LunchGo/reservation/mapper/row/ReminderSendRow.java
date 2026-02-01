package com.example.LunchGo.reservation.mapper.row;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;

@Getter
public class ReminderSendRow {
    private Long reservationId;
    private String userPhone;
    private String restaurantName;
    private LocalDate slotDate;
    private LocalTime slotTime;
}
