package com.example.LunchGo.reservation.repository;

import com.example.LunchGo.reservation.entity.ReservationCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationCancellationRepository extends JpaRepository<ReservationCancellation, Long> {
    boolean existsByReservationId(Long reservationId);
    java.util.Optional<ReservationCancellation> findByReservationId(Long reservationId);
}
