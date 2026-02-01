package com.example.LunchGo.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_restaurant_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyRestaurantStats {

    @EmbeddedId
    private DailyRestaurantStatsId id;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "try_count")
    private Integer tryCount;

    @Column(name = "confirm_count")
    private Integer confirmCount;

    @Column(name = "visit_count")
    private Integer visitCount;

    @Column(name = "defended_noshow_cnt")
    private Integer defendedNoshowCnt;

    @Column(name = "penalty_stl_amt")
    private Integer penaltyStlAmt;

    @Column(name = "revenue_total")
    private Long revenueTotal;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    @jakarta.persistence.Embeddable
    public static class DailyRestaurantStatsId implements Serializable {

        @Column(name = "stat_date", nullable = false)
        private LocalDate statDate;

        @Column(name = "restaurant_id", nullable = false)
        private Long restaurantId;
    }
}
