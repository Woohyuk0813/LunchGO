package com.example.LunchGo.restaurant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyAiInsightsResponse {
    private String startDate;
    private String endDate;
    private String predictionWeekStart;
    private String predictionWeekEnd;
    private String aiSummary;
    private boolean aiFallbackUsed;
    private List<DailyReservationPoint> reservations;
    private List<DailyStatsPoint> stats;
    private List<WeeklyDemandPrediction> predictions;
    private List<WeeklyPredictionPoint> lastWeekPredictions;
    private SignalSummary signalSummary;

    @Getter
    @Builder
    public static class DailyReservationPoint {
        private String date;
        private int count;
        private int amount;
    }

    @Getter
    @Builder
    public static class DailyStatsPoint {
        private String date;
        private int viewCount;
        private int tryCount;
        private int confirmCount;
        private int visitCount;
        private int noshowCount;
        private long penaltyAmount;
        private long revenue;
    }

    @Getter
    @Builder
    public static class SignalSummary {
        private int publicBookmarkCount;
        private int approvedLinkCount;
        private List<String> restaurantMenuKeywords;
        private long restaurantMenuOverlap;
        private List<String> menuKeywords;
        private List<String> preferenceKeywords;
        private long keywordOverlap;
        private List<String> mismatchDates;
        private List<String> noMenuDates;
        private String topCompanyName;
        private double topCompanyShare;
    }

    @Getter
    @Builder
    public static class WeeklyPredictionPoint {
        private String date;
        private int expectedMin;
        private int expectedMax;
        private int expectedAvg;
    }
}
