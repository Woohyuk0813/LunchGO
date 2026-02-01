package com.example.LunchGo.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "confirmed_amount", nullable = false)
    private Integer confirmedAmount;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Receipt(Long reservationId, Integer confirmedAmount, String imageUrl) {
        this.reservationId = reservationId;
        this.confirmedAmount = confirmedAmount;
        this.imageUrl = imageUrl;
    }

    public void updateConfirmedAmount(Integer confirmedAmount) {
        this.confirmedAmount = confirmedAmount;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
