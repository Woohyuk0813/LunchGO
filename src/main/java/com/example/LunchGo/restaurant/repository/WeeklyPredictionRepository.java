package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.WeeklyPrediction;
import com.example.LunchGo.restaurant.entity.WeeklyPrediction.WeeklyPredictionId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeeklyPredictionRepository extends JpaRepository<WeeklyPrediction, WeeklyPredictionId> {
    
    /**
     * 특정 식당의 특정 주에 대한 예측 데이터 조회
     */
    @Query(
        value = """
            SELECT * FROM weekly_predictions
            WHERE restaurant_id = :restaurantId
              AND week_start_date = :weekStartDate
            ORDER BY weekday ASC
            """,
        nativeQuery = true
    )
    List<WeeklyPrediction> findByRestaurantIdAndWeekStartDate(
        @Param("restaurantId") Long restaurantId,
        @Param("weekStartDate") LocalDate weekStartDate
    );

    /**
     * 특정 식당의 저번 주 예측 데이터 조회
     */
    @Query(
        value = """
            SELECT * FROM weekly_predictions
            WHERE restaurant_id = :restaurantId
              AND week_start_date = DATE_SUB(:currentWeekStart, INTERVAL 7 DAY)
            ORDER BY weekday ASC
            """,
        nativeQuery = true
    )
    List<WeeklyPrediction> findLastWeekPredictions(
        @Param("restaurantId") Long restaurantId,
        @Param("currentWeekStart") LocalDate currentWeekStart
    );
}

