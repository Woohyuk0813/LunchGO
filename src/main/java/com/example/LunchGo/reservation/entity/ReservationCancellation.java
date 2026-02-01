package com.example.LunchGo.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "reservation_cancellations")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReservationCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancellation_id")
    private Long cancellationId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "cancelled_by", nullable = false, length = 20)
    private String cancelledBy;

    @Column(name = "cancelled_by_id")
    private Long cancelledById;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @CreationTimestamp
    @Column(name = "cancelled_at", nullable = false, updatable = false)
    private LocalDateTime cancelledAt;
}
