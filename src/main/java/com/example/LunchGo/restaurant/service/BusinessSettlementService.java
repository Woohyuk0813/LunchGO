package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationDailyTypeStatsRow;
import com.example.LunchGo.restaurant.dto.MonthlySettlementResponse;
import com.example.LunchGo.restaurant.dto.MonthlySettlementResponse.DailyPoint;
import com.example.LunchGo.restaurant.entity.DailyRestaurantStats;
import com.example.LunchGo.restaurant.repository.DailyRestaurantStatsRepository;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;


@Service
@RequiredArgsConstructor
public class BusinessSettlementService {

    private final DailyRestaurantStatsRepository statsRepository;
    private final ReservationMapper reservationMapper;
    private final RestaurantRepository restaurantRepository;

    public MonthlySettlementResponse getLast30Days(Long restaurantId) {
        // 30일 매출/기본 시계열(날짜는 daily_restaurant_stats 기준)
        List<DailyRestaurantStats> statsRows = statsRepository.findLast30DaysByRestaurantId(restaurantId);

        // 30일 타입별 예약/취소 시계열
        List<ReservationDailyTypeStatsRow> typeRows = reservationMapper.selectLast30DaysReservationTypeStats(restaurantId);
        Map<String, ReservationDailyTypeStatsRow> typeByDate = new HashMap<>();
        for (ReservationDailyTypeStatsRow r : typeRows) {
            if (r.getDate() != null) {
                typeByDate.put(r.getDate().toString(), r);
            }
        }

        boolean preorderAvailable = restaurantRepository.findById(restaurantId)
                .map(rest -> rest.isPreorderAvailable())
                .orElse(false);

        int reservationCount = 0;
        int depositCount = 0;
        int preorderCount = 0;
        int cancellationCount = 0;
        long totalSales = 0;

        List<DailyPoint> daily = statsRows.stream().map(ds -> {
            String date = ds.getId().getStatDate().toString();
            long sales = ds.getRevenueTotal() == null ? 0 : ds.getRevenueTotal();

            ReservationDailyTypeStatsRow tr = typeByDate.get(date);
            int dep = tr == null || tr.getDepositCount() == null ? 0 : tr.getDepositCount();
            int pre = tr == null || tr.getPreorderCount() == null ? 0 : tr.getPreorderCount();
            int canc = tr == null || tr.getCancellationCount() == null ? 0 : tr.getCancellationCount();
            int total = tr == null || tr.getTotalCount() == null ? 0 : tr.getTotalCount();

            int cancRate = (total == 0) ? 0 : (int) Math.round((double) canc / total * 100);

            return new DailyPoint(
                    date,
                    total,
                    dep,
                    pre,
                    canc,
                    cancRate,
                    sales
            );
        }).toList();

        for (DailyPoint p : daily) {
            reservationCount += p.getReservations();
            depositCount += p.getDepositReservations();
            preorderCount += p.getPreorderReservations();
            cancellationCount += p.getCancellations();
            totalSales += p.getSales();
        }

        int totalCapacity = reservationMapper.sumMonthlyCapacityByRestaurant(restaurantId);
        int reservationRate = (totalCapacity == 0)
                ? 0
                : (int) Math.round((double) reservationCount / totalCapacity * 100);

        int cancellationRate = (reservationCount == 0)
                ? 0
                : (int) Math.round((double) cancellationCount / reservationCount * 100);

        return new MonthlySettlementResponse(
                reservationCount,
                totalSales,
                reservationRate,
                totalCapacity,
                preorderAvailable,
                depositCount,
                preorderCount,
                cancellationCount,
                cancellationRate,
                daily
        );
    }
}
