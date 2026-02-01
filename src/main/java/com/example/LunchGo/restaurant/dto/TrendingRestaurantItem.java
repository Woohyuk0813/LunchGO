package com.example.LunchGo.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TrendingRestaurantItem {
    private Long restaurantId;
    private String name;
    private String roadAddress;
    private String detailAddress;
    private int viewCount;
    private int confirmCount;
    private int reviewCount;
    private double rating;
    private double score;
    private String imageUrl;
    private List<TrendingTagItem> tags;
    private List<TrendingTagItem> reviewTags;
}
