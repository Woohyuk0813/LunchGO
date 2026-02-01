package com.example.LunchGo.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerReservationNotification {
    private String reservationCode;

    private String date;
    private String time;
    private Integer partySize;

    private String name; //예약자 이름
}
