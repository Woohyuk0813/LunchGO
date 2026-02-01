package com.example.LunchGo.review.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyReviewItem {
    private Long reviewId;
    private Long reservationId;
    private RestaurantInfo restaurant;
    private Integer visitCount;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDate visitDate;
    private String content;
    private String status;
    private List<String> tags;
    private List<String> images;

    @Getter
    @AllArgsConstructor
    public static class RestaurantInfo {
        private Long id;
        private String name;
        private String address;
    }
}
