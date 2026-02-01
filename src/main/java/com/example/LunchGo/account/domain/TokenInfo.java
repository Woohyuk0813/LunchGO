package com.example.LunchGo.account.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "MemberToken", timeToLive = 3600 * 24) //하루
@AllArgsConstructor
@Getter
@Setter
public class TokenInfo {

    @Id
    private String refreshToken;

    private Long id; //사용자 또는 사업자 pk

    private String role;
}
