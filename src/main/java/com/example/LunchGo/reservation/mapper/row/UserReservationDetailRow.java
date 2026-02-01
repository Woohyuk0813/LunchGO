package com.example.LunchGo.reservation.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReservationDetailRow {
    private Long reservationId;
    private String confirmationNumber;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    private String date;
    private String time;
    private Integer partySize;
    private String requestNote;
    private Integer amount;
    private String paymentType;
    private String paymentMethod;
    private String paidAt;
    private String status;
}
