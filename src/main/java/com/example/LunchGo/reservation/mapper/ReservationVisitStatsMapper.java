package com.example.LunchGo.reservation.mapper;

import com.example.LunchGo.reservation.mapper.row.RestaurantUserStatsRow;
import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationVisitStatsMapper {

    RestaurantUserStatsRow selectRestaurantUserStatsForUpdate(
        @Param("restaurantId") Long restaurantId,
        @Param("userId") Long userId
    );

    int upsertRestaurantUserStats(
        @Param("restaurantId") Long restaurantId,
        @Param("userId") Long userId,
        @Param("visitCount") Integer visitCount,
        @Param("lastVisitDate") LocalDate lastVisitDate
    );

    int insertReservationVisitStats(
        @Param("reservationId") Long reservationId,
        @Param("userId") Long userId,
        @Param("restaurantId") Long restaurantId,
        @Param("visitNumber") Integer visitNumber,
        @Param("prevVisitDate") LocalDate prevVisitDate,
        @Param("daysSinceLastVisit") Integer daysSinceLastVisit
    );
}
