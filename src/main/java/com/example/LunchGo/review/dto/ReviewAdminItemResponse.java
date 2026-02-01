package com.example.LunchGo.review.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewAdminItemResponse {
    private Long reviewId;
    private Long restaurantId;
    private String reviewerName;
    private String restaurantName;
    private Integer rating;
    private String content;
    private Integer totalAmount;
    private LocalDateTime createdAt;
    private String status;
    private Integer commentCount;
    private Long blindRequestTagId;
    private String blindRequestTagName;
    private String blindRequestReason;
    private LocalDateTime blindRequestedAt;
}
