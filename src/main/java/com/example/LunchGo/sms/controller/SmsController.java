package com.example.LunchGo.sms.controller;

import com.example.LunchGo.sms.dto.SmsDTO;
import com.example.LunchGo.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Log4j2
public class SmsController {
    private final SmsService smsService;

    @PostMapping("/sms/send")
    public ResponseEntity<?> sendSMS(@RequestBody SmsDTO smsDTO) {
        if(!StringUtils.hasLength(smsDTO.getPhone())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        smsService.certificateSMS(smsDTO.getPhone().replace("-", "")); //전화번호 양식 지우고 인증번호 발송
        //메시지 전송 실패시 500 에러 보냄
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<Boolean> verifySMS(@RequestBody SmsDTO smsDTO) {
        if(!StringUtils.hasLength(smsDTO.getPhone()) || !StringUtils.hasLength(smsDTO.getVerifyCode())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        log.info("들어온 전화번호: " + smsDTO.getPhone()+" 들어온 인증코드: "+smsDTO.getVerifyCode());
        Boolean result = smsService.verifySMSCode(smsDTO.getPhone().replace("-", ""), smsDTO.getVerifyCode());
        //인증코드 일치하면 true, 안맞으면 false
        return ResponseEntity.ok(result);
    }
}
