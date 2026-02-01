package com.example.LunchGo.account.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFindRequest {
    private String name;
    private String phone;
    private String verifyCode;
}
