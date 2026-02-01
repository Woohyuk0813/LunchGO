package com.example.LunchGo.reservation.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReservationListRow {
    private String id;
    private String restaurantName;
    private String customerName;
    private String reservationDateTime;
    private Integer partySize;
    private String type;
    private String status;
}
