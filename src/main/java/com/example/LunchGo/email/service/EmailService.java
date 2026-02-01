package com.example.LunchGo.email.service;

import com.example.LunchGo.email.dto.PromotionDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

public interface EmailService {
    String createCode();

    String setContext(String code);

    MimeMessage createEmailForm(String email) throws MessagingException;

    void sendEmail(String toEmail) throws MessagingException;

    Boolean verifyEmailCode(Long id, String email, String code);

    String setPromotionContext(String promotionContext);

    MimeMessage createPromotionEmail(String title, String content, String email) throws MessagingException;

    void checkAndLockPromotion(PromotionDTO promotionDTO);

    void sendPromotionAsync(List<String> emails, PromotionDTO promotionDTO);
}
