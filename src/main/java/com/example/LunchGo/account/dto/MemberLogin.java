package com.example.LunchGo.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberLogin {
    private Long id;
    private String email;
    private String password;
    private String role;
    private String name;
    private String image;
    private String accessToken;
}
