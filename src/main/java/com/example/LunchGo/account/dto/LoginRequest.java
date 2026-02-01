package com.example.LunchGo.account.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
    private String userType; // USER, OWNER, STAFF, MANAGER
    private String queueToken;
}
