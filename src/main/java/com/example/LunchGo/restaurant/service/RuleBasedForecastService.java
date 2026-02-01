package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.WeeklyDemandPrediction;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse.BaselinePoint;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse.CafeteriaMenuSignal;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse.CompanySignal;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse.PreferenceSignal;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse.ShareSignal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RuleBasedForecastService {

    @Value("${owner.ai.forecast.menu-factor.min:0.9}")
    private double menuFactorMin;

    @Value("${owner.ai.forecast.menu-factor.max:1.1}")
    private double menuFactorMax;

    @Value("${owner.ai.forecast.share-factor.max:1.1}")
    private double shareFactorMax;

    @Value("${owner.ai.forecast.company-factor.high:1.05}")
    private double companyFactorHigh;

    @Value("${owner.ai.forecast.company-factor.medium:1.02}")
    private double companyFactorMedium;

    @Value("${owner.ai.forecast.combined.min:0.9}")
    private double combinedFactorMin;

    @Value("${owner.ai.forecast.combined.max:1.1}")
    private double combinedFactorMax;

    @Value("${owner.ai.forecast.margin.ratio:0.1}")
    private double marginRatio;

    public List<WeeklyDemandPrediction> forecast(
        WeeklyDemandSignalResponse signal,
        LocalDate weekStart,
        LocalDate weekEnd
    ) {
        Map<Integer, BaselinePoint> baselineByWeekday = signal.getBaselineByWeekday().stream()
            .collect(Collectors.toMap(BaselinePoint::getWeekday, point -> point, (a, b) -> a));

        double menuMatchFactor = calculateMenuMatchFactor(
            signal.getCafeteriaMenuSignals(),
            signal.getPreferenceSignals()
        );
        Map<Integer, MenuMatchStats> menuMatchByWeekday = calculateMenuMatchByWeekday(
            signal.getCafeteriaMenuSignals(),
            signal.getPreferenceSignals()
        );
        Map<Integer, Boolean> menuAvailableByWeekday =
            buildMenuAvailabilityByWeekday(signal.getCafeteriaMenuSignals());
        double shareFactor = calculateShareFactor(signal.getShareSignal());
        double companyFactor = calculateCompanyFactor(signal.getCompanySignals());
        double combinedFactor = clamp(
            menuMatchFactor * shareFactor * companyFactor,
            combinedFactorMin,
            combinedFactorMax
        );

        return java.util.stream.IntStream.rangeClosed(1, 7)
            .mapToObj(weekday -> buildPredictionForWeekday(
                weekday,
                baselineByWeekday.get(weekday),
                combinedFactor,
                menuMatchByWeekday.getOrDefault(weekday, null),
                menuMatchFactor,
                menuAvailableByWeekday.getOrDefault(weekday, false),
                shareFactor,
                companyFactor,
                signal
            ))
            .toList();
    }

    private WeeklyDemandPrediction buildPredictionForWeekday(
        int weekday,
        BaselinePoint baseline,
        double combinedFactor,
        MenuMatchStats menuMatchStats,
        double fallbackMatchFactor,
        boolean menuAvailable,
        double shareFactor,
        double companyFactor,
        WeeklyDemandSignalResponse signal
    ) {
        double menuMatchFactor = menuMatchStats == null
            ? (menuAvailable ? fallbackMatchFactor : 1.0)
            : menuMatchStats.factor;
        double avg = baseline == null ? 0.0 : baseline.getAvgCount();
        double stddev = baseline == null ? 0.0 : baseline.getStddev();
        double expected = avg * combinedFactor;
        double margin = stddev > 0 ? stddev : Math.max(1.0, expected * marginRatio);

        int expectedMin = (int) Math.max(0, Math.round(expected - margin));
        int expectedMax = (int) Math.max(expectedMin, Math.round(expected + margin));

        String confidence = buildConfidence(avg, menuMatchFactor, shareFactor, companyFactor);

        java.util.ArrayList<String> evidence = new java.util.ArrayList<>();
        if (avg > 0) {
            evidence.add(String.format(Locale.KOREA, "최근 요일 평균 %.1f건", avg));
        } else {
            evidence.add("최근 요일 평균 데이터가 부족합니다.");
        }
        if (!signal.getCafeteriaMenuSignals().isEmpty()) {
            if (!menuAvailable) {
                evidence.add("구내식당 없음 (N/A)");
                evidence.add("구내식당 운영 부재로 외식 수요 증가 가능성");
            } else if (menuMatchStats != null) {
                evidence.add(String.format(
                    Locale.KOREA,
                    "구내식당/취향 매칭 지수 %.2f (%d/%d)",
                    menuMatchFactor,
                    menuMatchStats.matched,
                    menuMatchStats.total
                ));
                if (menuMatchStats.mismatch > 0) {
                    evidence.add(String.format(
                        Locale.KOREA,
                        "구내식당/취향 불일치 키워드 %d건",
                        menuMatchStats.mismatch
                    ));
                }
            } else {
                evidence.add(String.format(Locale.KOREA, "구내식당/취향 매칭 지수 %.2f", menuMatchFactor));
            }
        }
        if (signal.getShareSignal() != null) {
            evidence.add(String.format(
                Locale.KOREA,
                "공유 지표: 공개 북마크 %d, 공유 링크 %d",
                signal.getShareSignal().getPublicBookmarkCount(),
                signal.getShareSignal().getApprovedLinkCount()
            ));
        }
        CompanySignal topCompany = signal.getCompanySignals().stream()
            .findFirst()
            .orElse(null);
        if (topCompany != null && topCompany.getCompanyName() != null) {
            evidence.add(String.format(
                Locale.KOREA,
                "상위 회사 비중 %.0f%% (%s)",
                topCompany.getShare() * 100,
                topCompany.getCompanyName()
            ));
        }

        return WeeklyDemandPrediction.builder()
            .weekday(weekday)
            .expectedMin(expectedMin)
            .expectedMax(expectedMax)
            .confidence(confidence)
            .evidence(evidence)
            .build();
    }

    private double calculateMenuMatchFactor(
        List<CafeteriaMenuSignal> menus,
        List<PreferenceSignal> preferences
    ) {
        if (menus.isEmpty() || preferences.isEmpty()) {
            return 1.0;
        }
        Set<String> likedKeywords = preferences.stream()
            .filter(PreferenceSignal::isLiked)
            .map(PreferenceSignal::getKeyword)
            .filter(keyword -> keyword != null && !keyword.isBlank())
            .map(keyword -> keyword.trim().toLowerCase(Locale.KOREA))
            .collect(Collectors.toSet());

        if (likedKeywords.isEmpty()) {
            return 1.0;
        }

        int totalMenuNames = 0;
        int matched = 0;
        for (CafeteriaMenuSignal menu : menus) {
            List<String> menuNames = menu.getMenuNames();
            if (menuNames == null) {
                continue;
            }
            for (String name : menuNames) {
                if (name == null || name.isBlank()) {
                    continue;
                }
                totalMenuNames += 1;
                String normalized = name.trim().toLowerCase(Locale.KOREA);
                boolean hit = likedKeywords.stream().anyMatch(normalized::contains);
                if (hit) {
                    matched += 1;
                }
            }
        }
        if (totalMenuNames == 0) {
            return 1.0;
        }
        double ratio = (double) matched / totalMenuNames;
        return clamp(0.9 + 0.2 * ratio, menuFactorMin, menuFactorMax);
    }

    private Map<Integer, MenuMatchStats> calculateMenuMatchByWeekday(
        List<CafeteriaMenuSignal> menus,
        List<PreferenceSignal> preferences
    ) {
        if (menus.isEmpty() || preferences.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        Set<String> likedKeywords = preferences.stream()
            .filter(PreferenceSignal::isLiked)
            .map(PreferenceSignal::getKeyword)
            .filter(keyword -> keyword != null && !keyword.isBlank())
            .map(keyword -> keyword.trim().toLowerCase(Locale.KOREA))
            .collect(Collectors.toSet());

        if (likedKeywords.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        Set<String> dislikedKeywords = preferences.stream()
            .filter(pref -> !pref.isLiked())
            .map(PreferenceSignal::getKeyword)
            .filter(keyword -> keyword != null && !keyword.isBlank())
            .map(keyword -> keyword.trim().toLowerCase(Locale.KOREA))
            .collect(Collectors.toSet());

        Map<Integer, int[]> countsByWeekday = new java.util.HashMap<>();
        for (CafeteriaMenuSignal menu : menus) {
            if (menu.getServedDate() == null || menu.getMenuNames() == null) {
                continue;
            }
            LocalDate date = LocalDate.parse(menu.getServedDate());
            int normalizedWeekday = toMysqlWeekday(date);
            int[] counts = countsByWeekday.computeIfAbsent(normalizedWeekday, key -> new int[3]);
            for (String name : menu.getMenuNames()) {
                if (name == null || name.isBlank()) {
                    continue;
                }
                counts[0] += 1; // total
                String normalized = name.trim().toLowerCase(Locale.KOREA);
                boolean hit = likedKeywords.stream().anyMatch(normalized::contains);
                if (hit) {
                    counts[1] += 1; // matched
                }
                boolean mismatch = dislikedKeywords.stream().anyMatch(normalized::contains);
                if (mismatch) {
                    counts[2] += 1; // mismatch
                }
            }
        }

        Map<Integer, MenuMatchStats> result = new java.util.HashMap<>();
        countsByWeekday.forEach((weekday, counts) -> {
            if (counts[0] == 0) {
                return;
            }
            double ratio = (double) counts[1] / counts[0];
            double mismatchRatio = (double) counts[2] / counts[0];
            double factor = clamp(0.9 + 0.2 * ratio - 0.1 * mismatchRatio, menuFactorMin, menuFactorMax);
            result.put(weekday, new MenuMatchStats(factor, counts[0], counts[1], counts[2]));
        });
        return result;
    }

    private int toMysqlWeekday(LocalDate date) {
        int weekday = date.getDayOfWeek().getValue(); // 1=Mon..7=Sun
        return weekday == 7 ? 1 : weekday + 1; // to 1=Sun..7=Sat
    }

    private Map<Integer, Boolean> buildMenuAvailabilityByWeekday(List<CafeteriaMenuSignal> menus) {
        if (menus == null || menus.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        Map<Integer, Boolean> result = new java.util.HashMap<>();
        for (CafeteriaMenuSignal menu : menus) {
            if (menu.getServedDate() == null) {
                continue;
            }
            LocalDate date = LocalDate.parse(menu.getServedDate());
            int weekday = toMysqlWeekday(date);
            result.put(weekday, true);
        }
        return result;
    }

    private static class MenuMatchStats {
        private final double factor;
        private final int total;
        private final int matched;
        private final int mismatch;

        private MenuMatchStats(double factor, int total, int matched, int mismatch) {
            this.factor = factor;
            this.total = total;
            this.matched = matched;
            this.mismatch = mismatch;
        }
    }

    private double calculateShareFactor(ShareSignal shareSignal) {
        if (shareSignal == null) {
            return 1.0;
        }
        double factor = 1.0;
        if (shareSignal.getPublicBookmarkCount() > 0) {
            factor += Math.min(0.05, shareSignal.getPublicBookmarkCount() / 200.0);
        }
        if (shareSignal.getApprovedLinkCount() > 0) {
            factor += Math.min(0.05, shareSignal.getApprovedLinkCount() / 5000.0);
        }
        return clamp(factor, menuFactorMin, shareFactorMax);
    }

    private double calculateCompanyFactor(List<CompanySignal> companySignals) {
        if (companySignals == null || companySignals.isEmpty()) {
            return 1.0;
        }
        CompanySignal top = companySignals.get(0);
        double share = top.getShare();
        if (share >= 0.4) {
            return companyFactorHigh;
        }
        if (share >= 0.25) {
            return companyFactorMedium;
        }
        return 1.0;
    }

    private String buildConfidence(
        double avg,
        double menuMatchFactor,
        double shareFactor,
        double companyFactor
    ) {
        if (avg <= 0) {
            return "LOW";
        }
        double signalStrength = (menuMatchFactor + shareFactor + companyFactor) / 3.0;
        if (avg >= 8 && signalStrength >= 1.02) {
            return "HIGH";
        }
        if (avg >= 3) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
