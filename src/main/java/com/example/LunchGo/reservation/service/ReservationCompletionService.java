package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.mapper.ReservationVisitStatsMapper;
import com.example.LunchGo.reservation.mapper.row.RestaurantUserStatsRow;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCompletionService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationVisitStatsMapper reservationVisitStatsMapper;

    @Transactional
    public void complete(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

        if (ReservationStatus.COMPLETED.equals(reservation.getStatus())) {
            return;
        }

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
            .orElseThrow(() -> new IllegalArgumentException("예약 슬롯 정보를 찾을 수 없습니다."));

        Long userId = reservation.getUserId();
        Long restaurantId = slot.getRestaurantId();
        LocalDate slotDate = slot.getSlotDate();

        RestaurantUserStatsRow stats = reservationVisitStatsMapper.selectRestaurantUserStatsForUpdate(
            restaurantId,
            userId
        );

        int currentVisitCount = stats != null && stats.getVisitCount() != null
            ? stats.getVisitCount()
            : 0;
        LocalDate prevVisitDate = stats != null ? stats.getLastVisitDate() : null;

        int visitNumber = currentVisitCount + 1;
        Integer daysSinceLastVisit = prevVisitDate == null
            ? null
            : Math.toIntExact(ChronoUnit.DAYS.between(prevVisitDate, slotDate));

        reservationVisitStatsMapper.insertReservationVisitStats(
            reservationId,
            userId,
            restaurantId,
            visitNumber,
            prevVisitDate,
            daysSinceLastVisit
        );

        reservationVisitStatsMapper.upsertRestaurantUserStats(
            restaurantId,
            userId,
            visitNumber,
            slotDate
        );

        reservation.setStatus(ReservationStatus.COMPLETED);
    }
}
