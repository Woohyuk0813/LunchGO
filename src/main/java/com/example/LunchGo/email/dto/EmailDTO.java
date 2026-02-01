package com.example.LunchGo.email.dto;

import lombok.Data;

@Data
public class EmailDTO {
    private String mail;
    //메일 주소

    private String verifyCode;
    // 인증 코드
}
