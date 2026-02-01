package com.example.LunchGo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminReviewHideResponse {
    private Long reviewId;
    private String status;
}
