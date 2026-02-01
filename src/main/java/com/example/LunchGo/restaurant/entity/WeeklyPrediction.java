package com.example.LunchGo.restaurant.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "weekly_predictions")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WeeklyPrediction {

    @EmbeddedId
    private WeeklyPredictionId id;

    @Column(name = "expected_min", nullable = false)
    private Integer expectedMin;

    @Column(name = "expected_max", nullable = false)
    private Integer expectedMax;

    @Column(name = "confidence", nullable = false, length = 10)
    private String confidence; // LOW, MEDIUM, HIGH

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence; // JSON 문자열로 저장

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    @jakarta.persistence.Embeddable
    public static class WeeklyPredictionId implements Serializable {

        @Column(name = "restaurant_id", nullable = false)
        private Long restaurantId;

        @Column(name = "week_start_date", nullable = false)
        private LocalDate weekStartDate; // 예측한 주의 시작일 (월요일)

        @Column(name = "weekday", nullable = false)
        private Integer weekday; // 1=일요일, 2=월요일, ..., 7=토요일
    }
}

