package com.example.LunchGo.sms.dto;

import lombok.Data;

@Data
public class SmsDTO {
    //발신 번호
    private String phone;
    //인증번호
    private String verifyCode;
}
