package com.example.LunchGo.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessVisitNotificationResponse {
    private Long id;
    private Long reservationId;
    private String customerName;
    private String phone;
    private String reservationDatetime;
    private String messageSentAt;
    private String responseStatus;
    private String responseAt;
    private String responseNote;
    private Boolean isRead;
}
