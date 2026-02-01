package com.example.LunchGo.restaurant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RestaurantStatusMapper {

    /**
     * 특정 요일에 정기 휴무일인 식당의 상태를 대량으로 업데이트합니다.
     *
     * @param dayOfWeek MySQL DAYOFWEEK() 기준 요일 (1=일, 2=월, ..., 7=토)
     * @return 영향을 받은 행의 수
     */
    int updateHolidayRestaurants(@Param("dayOfWeek") int dayOfWeek);

    /**
     * 특정 요일에 정기 휴무일이 아닌 식당의 상태를 대량으로 업데이트합니다.
     *
     * @param dayOfWeek MySQL DAYOFWEEK() 기준 요일
     * @return 영향을 받은 행의 수
     */
    int updateNonHolidayRestaurants(@Param("dayOfWeek") int dayOfWeek);
}
