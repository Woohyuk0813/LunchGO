package com.example.LunchGo.review.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateReviewResponse {
    private Long reviewId;
    private LocalDateTime createdAt;
    private String status;
}
