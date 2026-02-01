package com.example.LunchGo.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortoneCompleteRequest {
    private String merchantUid;
    private String impUid;
    private Integer paidAmount;
}
