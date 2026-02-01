package com.example.LunchGo.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "blind_request_tag_id")
    private Long blindRequestTagId;

    @Column(name = "blind_request_reason")
    private String blindRequestReason;

    @Column(name = "blind_requested_at")
    private LocalDateTime blindRequestedAt;

    public Review(Long restaurantId, Long userId, Long receiptId, Long reservationId, Integer rating, String content) {
        this.restaurantId = restaurantId;
        this.userId = userId;
        this.receiptId = receiptId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.content = content;
    }

    public void updateContent(String content, Integer rating) {
        this.content = content;
        this.rating = rating;
    }

    public void updateReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public void requestBlind(Long tagId, String reason) {
        this.status = "BLIND_REQUEST";
        this.blindRequestTagId = tagId;
        this.blindRequestReason = reason;
        this.blindRequestedAt = LocalDateTime.now();
    }

    public void decideBlind(boolean approve) {
        this.status = approve ? "BLINDED" : "BLIND_REJECTED";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = "PUBLIC";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
