package com.example.LunchGo.restaurant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyDemandSignalResponse {
    private List<BaselinePoint> baselineByWeekday;
    private List<CafeteriaMenuSignal> cafeteriaMenuSignals;
    private List<String> restaurantMenuKeywords;
    private List<PreferenceSignal> preferenceSignals;
    private ShareSignal shareSignal;
    private List<CompanySignal> companySignals;

    @Getter
    @Builder
    public static class BaselinePoint {
        private int weekday; // 1=Sun..7=Sat (MySQL DAYOFWEEK)
        private double avgCount;
        private double stddev;
    }

    @Getter
    @Builder
    public static class CafeteriaMenuSignal {
        private Long userId;
        private String servedDate; // yyyy-MM-dd
        private List<String> menuNames;
        private String rawText;
    }

    @Getter
    @Builder
    public static class PreferenceSignal {
        private Long userId;
        private String keyword;
        private boolean liked;
    }

    @Getter
    @Builder
    public static class ShareSignal {
        private int publicBookmarkCount;
        private int approvedLinkCount;
    }

    @Getter
    @Builder
    public static class CompanySignal {
        private String companyName;
        private int visitorCount;
        private double share;
    }
}
