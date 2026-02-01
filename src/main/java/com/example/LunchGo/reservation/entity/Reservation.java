package com.example.LunchGo.reservation.entity;

import com.example.LunchGo.reservation.domain.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "reservation_code", nullable = false, length = 32)
    private String reservationCode;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Column(name = "reservation_type", nullable = false, length = 30)
    private String reservationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "request_message", length = 50)
    private String requestMessage;

    @Column(name = "deposit_amount")
    private Integer depositAmount;

    @Column(name = "prepay_amount")
    private Integer prepayAmount;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "payment_deadline_at")
    private LocalDateTime paymentDeadlineAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    @Column(name = "reminder_token", length = 64)
    private String reminderToken;
}
