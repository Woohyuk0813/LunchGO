package com.example.LunchGo.review.mapper.row;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyReviewRow {
    private Long reviewId;
    private Long reservationId;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private Integer visitCount;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDate visitDate;
    private String content;
    private String status;
    private String tags;
    private String images;
}
