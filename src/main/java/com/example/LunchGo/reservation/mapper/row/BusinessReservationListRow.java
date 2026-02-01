package com.example.LunchGo.reservation.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessReservationListRow {
    private Long id;
    private String name;
    private String phone;
    private String datetime;
    private Integer guests;
    private Integer amount;
    private String status;
}
