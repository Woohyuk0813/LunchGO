package com.example.LunchGo.reservation.mapper.row;

import lombok.Getter;

@Getter
public class BusinessVisitNotificationRow {
    private Long id;
    private Long reservationId;
    private String customerName;
    private String phone;
    private String reservationDatetime;
    private String messageSentAt;
    private String responseStatus;
    private String responseAt;
}
