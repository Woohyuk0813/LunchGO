package com.example.LunchGo.restaurant.mapper;

import com.example.LunchGo.restaurant.mapper.row.CafeteriaMenuSignalRow;
import com.example.LunchGo.restaurant.mapper.row.CompanyVisitorCountRow;
import com.example.LunchGo.restaurant.mapper.row.PreferenceSignalRow;
import com.example.LunchGo.restaurant.mapper.row.WeeklyBaselineRow;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OwnerAiInsightsMapper {

    List<WeeklyBaselineRow> selectWeeklyBaselineByWeekday(
        @Param("restaurantId") Long restaurantId,
        @Param("weekStart") LocalDate weekStart,
        @Param("weeksBack") int weeksBack
    );

    List<CafeteriaMenuSignalRow> selectCafeteriaMenusForWeek(
        @Param("weekStart") LocalDate weekStart,
        @Param("weekEnd") LocalDate weekEnd
    );

    List<PreferenceSignalRow> selectPreferenceKeywords();

    List<String> selectRestaurantMenuKeywords(@Param("restaurantId") Long restaurantId);

    int selectPublicBookmarkCount(@Param("restaurantId") Long restaurantId);

    int selectApprovedBookmarkLinkCount();

    List<CompanyVisitorCountRow> selectCompanyVisitorCounts(
        @Param("restaurantId") Long restaurantId,
        @Param("weekStart") LocalDate weekStart,
        @Param("weeksBack") int weeksBack
    );
}
