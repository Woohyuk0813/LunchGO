package com.example.LunchGo.reservation.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReservationListRow {
    private Long reservationId;
    private Long restaurantId;
    private String confirmationNumber;
    private String restaurantName;
    private String date;
    private String time;
    private Integer partySize;
    private String status;
    private String requestNote;
}
