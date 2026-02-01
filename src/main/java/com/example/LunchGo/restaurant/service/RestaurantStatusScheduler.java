package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.domain.RestaurantStatus;
import com.example.LunchGo.restaurant.mapper.RestaurantStatusMapper;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantStatusScheduler {

    private final RestaurantStatusMapper restaurantStatusMapper;

    /**
     * 매일 오전 6시에 식당의 영업 상태를 업데이트합니다.
     * - 정기 휴무일인 식당은 'CLOSED'로 변경합니다.
     * - 나머지 영업 가능한 식당은 'OPEN'으로 변경합니다.
     * - 'DELETED' 상태의 식당은 변경 대상에서 제외합니다.
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    @Transactional
    public void updateRestaurantStatus() {
        log.info("매일 오전 6시 식당 영업상태 업데이트 스케줄러 시작 (MyBatis)");

        DayOfWeek today = LocalDate.now(ZoneId.of("Asia/Seoul")).getDayOfWeek();
        // MySQL DAYOFWEEK() 함수 기준 (일요일=1, 월요일=2 ... 토요일=7)으로 변환
        int todayValue = today.getValue() % 7 + 1;

        int closedCount = restaurantStatusMapper.updateHolidayRestaurants(todayValue);
        int openCount = restaurantStatusMapper.updateNonHolidayRestaurants(todayValue);

        if (closedCount > 0 || openCount > 0) {
            log.info("식당 영업상태 업데이트 완료. OPEN으로 변경: {}곳, CLOSED로 변경: {}곳", openCount, closedCount);
        } else {
            log.info("식당 영업상태 변경 사항 없음.");
        }
    }
}
