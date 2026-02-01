package com.example.LunchGo.restaurant.repository;

public interface TrendingRestaurantProjection {
    Long getRestaurantId();
    String getName();
    String getRoadAddress();
    String getDetailAddress();
    Integer getViewCount();
    Integer getConfirmCount();
    Integer getReviewCount();
    Double getRating();
    Double getScore();
    String getImageUrl();
    String getTagIds();
    String getTagContents();
    String getReviewTagIds();
    String getReviewTagContents();
    String getReviewTagCounts();
}
