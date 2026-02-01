package com.example.LunchGo.sms.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.dto.OwnerReservationNotification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisUtil redisUtil;

    @Value("${coolsms.apiKey}")
    private String apiKey;

    @Value("${coolsms.secretkey}")
    private String apiSecret;

    @Value("${coolsms.from}") // 발신번호
    private String fromNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @Override
    public String createVerifyCode() {
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

    @Override
    public void certificateSMS(String phone) {
        //랜덤 번호 생성
        String randomNum = createVerifyCode();

        //메시지 생성
        Message coolsms = new Message();
        coolsms.setFrom(fromNumber);
        coolsms.setTo(phone);
        coolsms.setText("[LunchGo] 인증번호 [" + randomNum + "]를 입력해 주세요.");

        if(redisUtil.existData(phone)){
            redisUtil.deleteData(phone); //이미 존재하면 데이터 지우고 다시 보내기
        }

        long expireTime = 1000*60*3L; //유효기간 설정 후 redis에 저장
        redisUtil.setDataExpire(phone, randomNum, expireTime);

        try { //인증번호 전송
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(coolsms));
        }catch(Exception e) {
            redisUtil.deleteData(phone);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()); //메시지 전송 실패시 서버 문제
        }
    }

    //인증번호 검증
    @Override
    public Boolean verifySMSCode(String phone, String code) {
        String codeByPhone = redisUtil.getData(phone);

        if(codeByPhone == null) return false;
        return code.equals(codeByPhone);
    }

    @Override
    public void sendNotificationToOwner(String phone, OwnerReservationNotification notification) {
        Message notifyMessage = new Message(); //새로운 메시지 생성
        notifyMessage.setFrom(fromNumber);
        notifyMessage.setTo(phone);
        notifyMessage.setText("[LunchGo] 신규 예약 알림\n예약 코드: "+notification.getReservationCode()+"\n예약자: "+notification.getName()+
                "\n예약일: "+notification.getDate()+" "+notification.getTime()+"\n인원수: "+notification.getPartySize()+
                "\n\n상세 내용은 LunchGo에서 확인해주세요.");

        if(redisUtil.existData(notification.getReservationCode())) return; //예약 코드가 unique

        long expireTime = 1000*60*1L; //중복발신 제거를 위해 유효시간 1분 정해둠
        redisUtil.setDataExpire(notification.getReservationCode(), phone, expireTime);

        try { //인증번호 전송
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(notifyMessage));
        }catch(Exception e) {
            redisUtil.deleteData(phone);
        }
    }

    @Override
    public void sendRestaurantRegistrationNotice(List<String> ownerPhones) {
        String content = "[LunchGO] 미등록시 계정 휴면됩니다. 사업자페이지>식당정보 메뉴에서 등록 부탁드립니다.";

        for (String phone : ownerPhones) {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(phone);
            message.setText(content);

            // Redis Key 충돌 방지를 위한 접두사 사용
            String redisKey = "notif:reg:" + phone;

            // 중복 발송 방지 (이미 키가 존재하면 스킵)
            if (redisUtil.existData(redisKey)) {
                continue;
            }

            // 3일간 동일 번호로의 중복 발송 방지
            long expireTime = 1000 * 60 * 60 * 24 * 3L;
            redisUtil.setDataExpire(redisKey, "sent", expireTime);

            try {
                SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            } catch (Exception e) {
                // 개별 발송 실패 시 해당 번호의 Redis 데이터 삭제 (재시도 가능하게 함)
                redisUtil.deleteData(redisKey);
                // 로그를 남기거나 예외를 던질 수 있으나, 전체 발송 중단을 피하기 위해 여기서는 catch만 함
            }
        }
    }
    @Override
    public void sendSystemSms(String to, String text) {
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);
        message.setText(text);

        try {
            this.messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            System.err.println("SMS sending failed to " + to + ": " + e.getMessage());
        }
    }
}
