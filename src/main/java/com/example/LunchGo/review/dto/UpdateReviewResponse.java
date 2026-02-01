package com.example.LunchGo.review.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateReviewResponse {
    private Long reviewId;
    private LocalDateTime updatedAt;
    private String status;
}
