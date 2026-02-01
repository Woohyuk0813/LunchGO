package com.example.LunchGo.review.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewBlindResponse {
    private Long reviewId;
    private String status;
    private Long blindRequestTagId;
    private String blindRequestReason;
    private LocalDateTime blindRequestedAt;
}
