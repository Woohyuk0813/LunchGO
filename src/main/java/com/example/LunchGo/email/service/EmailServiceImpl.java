package com.example.LunchGo.email.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.email.dto.PromotionDTO;
import com.example.LunchGo.member.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;
    private final Executor taskExecutor;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public String createCode() {
        int leftLimit = 48; //'0'
        int rightLimit = 122; // 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit+1)
                .filter(i-> (i <= 57 || i >= 65) && (i <= 90 || i>= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    //이메일 내용 초기화
    @Override
    public String setContext(String code) {
        Context context = new Context();

        context.setVariable("code", code);
        //스프링부트가 주입받은 엔진으로 resources/templates/mail.html 파일 찾음
        return templateEngine.process("mail", context);
    }

    @Override
    public MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = createCode();

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[LunchGo] 인증번호 입니다. 인증번호 입력란에 입력해주세요.");
        message.setFrom(senderEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 1000* 60*3L);
        return message;
    }

    //이메일 폼 생성 및 이메일 보내기
    @Override
    public void sendEmail(String toEmail) throws MessagingException {
        if(redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        MimeMessage emailForm = createEmailForm(toEmail);
        javaMailSender.send(emailForm);
    }

    //코드 검증
    @Override
    public Boolean verifyEmailCode(Long id, String email, String code) {
        String codeFoundByEmail = redisUtil.getData(email);
        log.info("code found by email: "+ codeFoundByEmail);
        if(codeFoundByEmail == null) return false; //코드가 null인 경우
        if (codeFoundByEmail.equals(code)) {
            redisUtil.deleteData(email);
            taskExecutor.execute(() -> { //일단 응답 먼저 반환 후 DB 반영
                TransactionTemplate template = new TransactionTemplate(transactionManager);
                template.executeWithoutResult(status -> userRepository.updateEmailAuthentication(id));
            });
            return true;
        }
        return false;
    }

    /**
     * 프로모션 보내기
     * */

    //프로모션 내용 설정
    @Override
    public String setPromotionContext(String promotionContext) {
        Context context = new Context();

        context.setVariable("promotionContext", promotionContext);

        return templateEngine.process("promotion", context);
    }

    //프로모션 내용 등록
    @Override
    public MimeMessage createPromotionEmail(String title, String content, String email) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email); //보낼 사람
        message.setSubject(title); //제목
        message.setFrom(senderEmail);
        message.setText(content, "utf-8", "html");

        return message;
    }

    @Override
    @Transactional
    public void checkAndLockPromotion(PromotionDTO promotionDTO) {
        if(redisUtil.existData(String.valueOf(promotionDTO.getOwnerId()))) { //이미 프로모션을 보낸 경우
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "프로모션은 6시간마다 한번 입력 가능합니다");
        }

        redisUtil.setDataExpire(String.valueOf(promotionDTO.getOwnerId()), "promotion", 1000 * 60 * 60 * 6L);
    }

    @Override
    @Async("taskExecutor")
    public void sendPromotionAsync(List<String> emails, PromotionDTO promotionDTO) {
        try{
            String htmlContent = setPromotionContext(promotionDTO.getContent());

            for(String email : emails) {
                MimeMessage emailForm = createPromotionEmail(promotionDTO.getTitle(), htmlContent, email);
                javaMailSender.send(emailForm);
            }
        }catch(Exception e){
            log.error("메일 발송 중 오류 발생, 해당 ownerId: "+ promotionDTO.getOwnerId());
        }
    }
}
