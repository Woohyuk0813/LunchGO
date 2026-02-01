package com.example.LunchGo.sms.service;

import com.example.LunchGo.reservation.dto.OwnerReservationNotification;

import java.util.List;

public interface SmsService {
    String createVerifyCode();

    void certificateSMS(String phone);

    Boolean verifySMSCode(String phone, String code);

    void sendNotificationToOwner(String phone, OwnerReservationNotification notification);

    void sendRestaurantRegistrationNotice(List<String> ownerPhones);

    void sendSystemSms(String to, String text);
}
