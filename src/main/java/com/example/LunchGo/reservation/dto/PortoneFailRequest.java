package com.example.LunchGo.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortoneFailRequest {
    private Long reservationId;
    private String reason;
}
