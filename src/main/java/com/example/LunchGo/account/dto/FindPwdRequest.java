package com.example.LunchGo.account.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPwdRequest {
    private String email; //사용자용
    private String loginId; //사업자용
    private String name;
    private String phone;
    private String password;
}
