package com.example.LunchGo.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {
    private String grantType; // "Bearer"
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
}
