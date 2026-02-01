package com.example.LunchGo.bookmark.repository;

public interface SharedBookmarkRow {
    Long getRestaurantId();
    String getName();
    String getRoadAddress();
    String getDetailAddress();
    String getImageUrl();
    Double getRating();
    Long getReviewCount();
}
