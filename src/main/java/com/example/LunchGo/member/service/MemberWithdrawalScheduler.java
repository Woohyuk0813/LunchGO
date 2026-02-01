package com.example.LunchGo.member.service;

import com.example.LunchGo.member.repository.OwnerRepository;
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
public class MemberWithdrawalScheduler {
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    @Scheduled(cron = "0 2 0 * * *") //매일 오전 12시 2분
    @Transactional
    public void deleteWithdrawalMembers() {
        LocalDateTime threshold = LocalDateTime.now().minusYears(2);
        //현재 기준 2년전 날짜 계산

        userRepository.deleteUserComplete(threshold); //사용자
        ownerRepository.deleteOwnerComplete(threshold); //사업자

        log.info("탈퇴 회원 DB 영구 삭제 완료");
    }
}
