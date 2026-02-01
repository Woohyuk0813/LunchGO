package com.example.LunchGo.cafeteria.repository;

public interface CafeteriaRestaurantProjection {
    Long getRestaurantId();
    String getName();
    String getRoadAddress();
    String getDetailAddress();
    Integer getAvgMainPrice();
    Double getRating();
    Long getReviewCount();
    String getImageUrl();
    String getTagContents();
    String getReviewTagContents();
    String getReviewTagCounts();
}
