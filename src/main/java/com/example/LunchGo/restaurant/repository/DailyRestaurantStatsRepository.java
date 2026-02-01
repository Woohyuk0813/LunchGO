package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.DailyRestaurantStats;
import com.example.LunchGo.restaurant.entity.DailyRestaurantStats.DailyRestaurantStatsId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyRestaurantStatsRepository extends JpaRepository<DailyRestaurantStats, DailyRestaurantStatsId> {
    @Query(
        value = """
            SELECT
              stat_date,
              restaurant_id,
              view_count,
              try_count,
              confirm_count,
              visit_count,
              defended_noshow_cnt,
              penalty_stl_amt,
              revenue_total
            FROM daily_restaurant_stats
            WHERE restaurant_id = :restaurantId
              AND stat_date BETWEEN CURDATE() - INTERVAL 6 DAY AND CURDATE()
            ORDER BY stat_date ASC
            """,
        nativeQuery = true
    )
    List<DailyRestaurantStats> findLast7DaysByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query(
            value = """
        SELECT
          stat_date,
          restaurant_id,
          view_count,
          try_count,
          confirm_count,
          visit_count,
          defended_noshow_cnt,
          penalty_stl_amt,
          revenue_total
        FROM daily_restaurant_stats
        WHERE restaurant_id = :restaurantId
          AND stat_date BETWEEN CURDATE() - INTERVAL 29 DAY AND CURDATE()
        ORDER BY stat_date ASC
        """,
            nativeQuery = true
    )
    List<DailyRestaurantStats> findLast30DaysByRestaurantId(@Param("restaurantId") Long restaurantId);

}
