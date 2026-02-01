package com.example.LunchGo.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReservationResponse {

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
