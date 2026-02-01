package com.example.LunchGo.restaurant.repository;

public interface RestaurantSimilarityProjection {
    Long getRestaurantId();
    Double getLikeScore();
    Double getDislikePenalty();
    Double getFinalScore();
}
