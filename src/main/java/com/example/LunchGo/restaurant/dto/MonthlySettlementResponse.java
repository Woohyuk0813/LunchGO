package com.example.LunchGo.restaurant.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlySettlementResponse {

    private int reservationCount;   // 30일 예약(임시/만료 제외) 합
    private long totalSales;        // 30일 매출 합
    private int reservationRate;    // reservationCount / capacity * 100
    private int totalCapacity;      // 30일 슬롯 max_capacity 합

    private boolean preorderAvailable; // 식당 선주문/선결제 기능 ON 여부
    private int depositCount;          // 30일 예약하기(예약금) 건수
    private int preorderCount;         // 30일 선주문/선결제 건수
    private int cancellationCount;     // 30일 취소(환불 포함) 건수
    private int cancellationRate;      // 30일 취소율(%) = cancellationCount / reservationCount * 100

    private List<DailyPoint> daily; // 그래프용 30일 시계열

    @Getter
    @AllArgsConstructor
    public static class DailyPoint {
        private String date;            // yyyy-MM-dd

        // 기존 필드 유지(전체 예약 수)
        private int reservations;       // (deposit + preorder) (임시/만료 제외)

        // 추가: 타입별 예약 수
        private int depositReservations;   // 예약하기(예약금)
        private int preorderReservations;  // 선주문/선결제

        // 추가: 취소
        private int cancellations;      // 취소(환불 포함)
        private int cancellationRate;   // 취소율(%) = cancellations / reservations * 100

        private long sales;             // revenue_total
    }
}
