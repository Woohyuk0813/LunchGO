package com.example.LunchGo.restaurant.stats;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DailyRestaurantStatsJdbcRepository {
    private static final String UPSERT_SQL =
            "INSERT INTO daily_restaurant_stats (stat_date, restaurant_id, view_count, confirm_count) " +
            "VALUES (?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "view_count = view_count + VALUES(view_count), " +
            "confirm_count = confirm_count + VALUES(confirm_count)";

    private final JdbcTemplate jdbcTemplate;

    public void upsertViewCounts(LocalDate date, List<Map.Entry<Long, Long>> entries) {
        if (entries.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                UPSERT_SQL,
                entries,
                entries.size(),
                (ps, entry) -> {
                    ps.setDate(1, Date.valueOf(date));
                    ps.setLong(2, entry.getKey());
                    ps.setLong(3, entry.getValue());
                    ps.setLong(4, 0L);
                }
        );
    }

    public void upsertConfirmCounts(LocalDate date, List<Map.Entry<Long, Long>> entries) {
        if (entries.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                UPSERT_SQL,
                entries,
                entries.size(),
                (ps, entry) -> {
                    ps.setDate(1, Date.valueOf(date));
                    ps.setLong(2, entry.getKey());
                    ps.setLong(3, 0L);
                    ps.setLong(4, entry.getValue());
                }
        );
    }
}
