package com.example.LunchGo.account.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class OwnerJoinRequest {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private LocalDate startAt;
    private String businessNum;
}
