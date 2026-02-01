package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.RegularHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegularHolidayRepository extends JpaRepository<RegularHoliday, Long> {

    /**
     * 특정 식당 ID에 속하는 모든 정기 휴무일을 조회합니다.
     * Spring Data JPA의 쿼리 메소드 기능을 활용합니다.
     * @param restaurantId 식당 ID
     * @return 해당 식당의 정기 휴무일 목록
     */
    List<RegularHoliday> findAllByRestaurantId(Long restaurantId);

    /**
     * 특정 식당 ID에 속하는 모든 정기 휴무일을 삭제합니다.
     * @param restaurantId 식당 ID
     */
    @Modifying
    @Query("DELETE FROM RegularHoliday rh WHERE rh.restaurantId = :restaurantId")
    void deleteAllByRestaurantId(@Param("restaurantId") Long restaurantId);

}
