package com.example.LunchGo.reservation.mapper.row;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.domain.ReservationType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateRow {

    private Long reservationId;
    private String reservationCode;

    private Long slotId;
    private Long userId;

    private Long restaurantId;
    private LocalDate slotDate;
    private LocalTime slotTime;

    private Integer partySize;
    private ReservationType reservationType;
    private ReservationStatus status;

    private String requestMessage;
}
