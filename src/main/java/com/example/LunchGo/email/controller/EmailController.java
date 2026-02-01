package com.example.LunchGo.email.controller;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.email.dto.EmailDTO;
import com.example.LunchGo.email.dto.PromotionDTO;
import com.example.LunchGo.email.service.EmailService;
import com.example.LunchGo.member.service.MemberService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmailController {
    private final EmailService emailService;
    private final MemberService memberService;

    @PostMapping("/email/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailDTO emailDTO) {
        if(!StringUtils.hasLength(emailDTO.getMail())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST); //필수 요소
        try {
            emailService.sendEmail(emailDTO.getMail());
        }catch(MessagingException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); //메일이 안보내질 경우 500 에러 처리
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Boolean> verifyEmail(@RequestBody EmailDTO emailDTO, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if(!StringUtils.hasLength(emailDTO.getMail()) || !StringUtils.hasLength(emailDTO.getVerifyCode())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (customUserDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(customUserDetails.getRole() == null || !customUserDetails.getRole().equals("ROLE_USER")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); //권한이 다르면 403
        }
        boolean isVerify = emailService.verifyEmailCode(customUserDetails.getId(),emailDTO.getMail(), emailDTO.getVerifyCode());
        return ResponseEntity.ok(isVerify);
    }

    @PostMapping("/business/promotion")
    public ResponseEntity<?> promotion(@RequestBody PromotionDTO promotionDTO) {
        if(!StringUtils.hasLength(promotionDTO.getTitle()) || !StringUtils.hasLength(promotionDTO.getContent())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        emailService.checkAndLockPromotion(promotionDTO); //6시간 안에 프로모션 보내면 에러 발생
        //프로모션 보내려면 즐겨찾기한 사용자 email 필수
        List<String> emails = memberService.getEmails(promotionDTO.getOwnerId()); //사업자 ID 잘못되면 404
        emailService.sendPromotionAsync(emails ,promotionDTO); //개발자 로그 확인 필수
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
