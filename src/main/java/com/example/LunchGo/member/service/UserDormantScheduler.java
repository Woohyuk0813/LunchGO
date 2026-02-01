package com.example.LunchGo.member.service;

import com.example.LunchGo.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserDormantScheduler {
    private final UserRepository userRepository;

    @Scheduled(cron = "0 1 0 * * *") //매일 오전 12시 1분
    @Transactional
    public void checkAndChangeDormantUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        //현재 기준 6개월 전 날짜 계산

        int updatedCount = userRepository.updateDormantUsers(threshold);

        log.info("휴면 계정 전환 완료: 총 {} 명의 사용자가 휴면 상태로 변경", updatedCount);
    }
}
