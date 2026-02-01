package com.example.LunchGo.account.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequest {
    private String email;
    private String password;
    private String name;
    private String companyName;
    private String companyAddress;
    private String phone;
    private Boolean marketingAgree;
}
