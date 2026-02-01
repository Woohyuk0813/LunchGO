package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse;
import com.example.LunchGo.restaurant.mapper.OwnerAiInsightsMapper;
import com.example.LunchGo.restaurant.mapper.row.CafeteriaMenuSignalRow;
import com.example.LunchGo.restaurant.mapper.row.CompanyVisitorCountRow;
import com.example.LunchGo.restaurant.mapper.row.PreferenceSignalRow;
import com.example.LunchGo.restaurant.mapper.row.WeeklyBaselineRow;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeeklyDemandSignalService {

    private final OwnerAiInsightsMapper insightsMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeeklyDemandSignalResponse buildWeeklySignal(
        Long restaurantId,
        LocalDate weekStart,
        LocalDate weekEnd
    ) {
        int weeksBack = 12;
        List<WeeklyBaselineRow> baselineRows =
            insightsMapper.selectWeeklyBaselineByWeekday(restaurantId, weekStart, weeksBack);
        List<CafeteriaMenuSignalRow> cafeteriaRows =
            insightsMapper.selectCafeteriaMenusForWeek(weekStart, weekEnd);
        List<PreferenceSignalRow> preferenceRows =
            insightsMapper.selectPreferenceKeywords();
        List<String> restaurantMenuKeywords =
            insightsMapper.selectRestaurantMenuKeywords(restaurantId);
        int publicBookmarkCount = insightsMapper.selectPublicBookmarkCount(restaurantId);
        int approvedLinkCount = insightsMapper.selectApprovedBookmarkLinkCount();
        List<CompanyVisitorCountRow> companyRows =
            insightsMapper.selectCompanyVisitorCounts(restaurantId, weekStart, weeksBack);

        int totalCompanyVisitors = companyRows.stream()
            .mapToInt(CompanyVisitorCountRow::getVisitorCount)
            .sum();

        return WeeklyDemandSignalResponse.builder()
            .baselineByWeekday(
                baselineRows.stream()
                    .map(row -> WeeklyDemandSignalResponse.BaselinePoint.builder()
                        .weekday(row.getWeekday())
                        .avgCount(row.getAvgCount())
                        .stddev(row.getStddev())
                        .build())
                    .toList())
            .cafeteriaMenuSignals(
                cafeteriaRows.stream()
                    .map(row -> WeeklyDemandSignalResponse.CafeteriaMenuSignal.builder()
                        .userId(row.getUserId())
                        .servedDate(Optional.ofNullable(row.getServedDate())
                            .map(LocalDate::toString)
                            .orElse(null))
                        .menuNames(parseMenuNames(row.getMainMenuNames()))
                        .rawText(row.getRawText())
                        .build())
                    .toList())
            .restaurantMenuKeywords(restaurantMenuKeywords)
            .preferenceSignals(
                preferenceRows.stream()
                    .map(row -> WeeklyDemandSignalResponse.PreferenceSignal.builder()
                        .userId(row.getUserId())
                        .keyword(row.getKeyword())
                        .liked(row.isLiked())
                        .build())
                    .toList())
            .shareSignal(WeeklyDemandSignalResponse.ShareSignal.builder()
                .publicBookmarkCount(publicBookmarkCount)
                .approvedLinkCount(approvedLinkCount)
                .build())
            .companySignals(
                companyRows.stream()
                    .map(row -> WeeklyDemandSignalResponse.CompanySignal.builder()
                        .companyName(row.getCompanyName())
                        .visitorCount(row.getVisitorCount())
                        .share(totalCompanyVisitors == 0
                            ? 0.0
                            : (double) row.getVisitorCount() / totalCompanyVisitors)
                        .build())
                    .toList())
            .build();
    }

    private List<String> parseMenuNames(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
