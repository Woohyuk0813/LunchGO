package com.example.LunchGo.reservation.repository;

import com.example.LunchGo.reservation.entity.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {
}
