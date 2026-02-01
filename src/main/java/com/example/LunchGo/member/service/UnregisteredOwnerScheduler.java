package com.example.LunchGo.member.service;

import com.example.LunchGo.member.repository.OwnerRepository;
import com.example.LunchGo.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnregisteredOwnerScheduler {

    private final OwnerRepository ownerRepository;
    private final SmsService smsService;

    /**
     * 매일 오전 12시 3분마다 식당 미등록 사업자에게 안내 문자 발송
     * 대상: 가입 후 4일 ~ 6일이 지난 활성 상태(ACTIVE)의 사업자 중 식당을 등록하지 않은 경우
     */
    @Scheduled(cron = "0 3 0 * * *") // 매일 오전 12시 3분 실행
    public void sendRestaurantRegistrationReminder() {
        log.info("[UnregisteredOwnerScheduler] Start sending restaurant registration reminders.");

        // 가입 후 4일 ~ 6일이 지난 대상 조회 (예: 6일 전 <= 가입일 <= 4일 전)
        LocalDateTime endDateTime = LocalDateTime.now().minusDays(4);
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(6);

        List<String> ownerPhones = ownerRepository.findPhonesByActiveAndNoRestaurant(startDateTime, endDateTime);

        if (ownerPhones.isEmpty()) {
            log.info("[UnregisteredOwnerScheduler] No owners found for registration reminder.");
            return;
        }

        log.info("[UnregisteredOwnerScheduler] Found {} owners. Sending SMS...", ownerPhones.size());

        // 배포 환경에서 동작하기 때문에, 일단 주석 처리하여 문자가 의도치 않게 발송되는 것을 방지
        try {
            // smsService.sendRestaurantRegistrationNotice(ownerPhones);
            log.info("[UnregisteredOwnerScheduler] SMS sending completed.");
        } catch (Exception e) {
            log.error("[UnregisteredOwnerScheduler] Failed to send SMS.", e);
        }
    }

    /**
     * 매일 오전 12시 3분마다 장기 미등록 사업자 자동 탈퇴 처리
     * 대상: 가입 후 1주일 이상 지난 활성 상태(ACTIVE)의 사업자 중 식당을 등록하지 않은 경우
     */
    @Transactional
    @Scheduled(cron = "0 3 0 * * *") // 매일 오전 12시 3분 실행
    public void withdrawUnregisteredOwners() {
        log.info("[UnregisteredOwnerScheduler] Start withdrawing unregistered owners.");

        // 가입 후 1주일(7일)이 지난 시점
        LocalDateTime targetTime = LocalDateTime.now().minusWeeks(1);

        List<Long> ownerIds = ownerRepository.findOwnerIdsByActiveAndNoRestaurantAndExpired(targetTime);

        if (ownerIds.isEmpty()) {
            log.info("[UnregisteredOwnerScheduler] No owners found for automatic withdrawal.");
            return;
        }

        log.info("[UnregisteredOwnerScheduler] Found {} owners to withdraw. Processing...", ownerIds.size());

        int successCount = 0;
        for (Long ownerId : ownerIds) {
            try {
                ownerRepository.withdrawOwner(ownerId);
                successCount++;
                log.info("[UnregisteredOwnerScheduler] Owner ID {} has been withdrawn.", ownerId);
            } catch (Exception e) {
                log.error("[UnregisteredOwnerScheduler] Failed to withdraw Owner ID {}.", ownerId, e);
            }
        }

        log.info("[UnregisteredOwnerScheduler] Withdrawal process completed. Success: {}, Total: {}", successCount, ownerIds.size());
    }
}
