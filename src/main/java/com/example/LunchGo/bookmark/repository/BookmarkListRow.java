package com.example.LunchGo.bookmark.repository;

public interface BookmarkListRow {
    Long getBookmarkId();
    Long getRestaurantId();
    String getName();
    String getDescription();
    Integer getAvgMainPrice();
    Integer getReservationLimit();
    Integer getPromotionAgree();
    Integer getIsPublic();
    String getImageUrl();
    Double getRating();
    Long getReviewCount();
}
