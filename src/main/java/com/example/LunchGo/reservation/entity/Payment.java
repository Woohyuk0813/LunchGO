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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "method", nullable = false, length = 20)
    private String method;

    @Column(name = "card_type", nullable = false, length = 20)
    private String cardType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "pg_provider", nullable = false, length = 50)
    private String pgProvider;

    @Column(name = "pg_payment_key", length = 128)
    private String pgPaymentKey;

    @Column(name = "pg_order_id", length = 64)
    private String pgOrderId;

    @Column(name = "merchant_uid", length = 64)
    private String merchantUid;

    @Column(name = "imp_uid", length = 64)
    private String impUid;

    @Column(name = "pg_tid", length = 64)
    private String pgTid;

    @Column(name = "receipt_url", length = 255)
    private String receiptUrl;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
