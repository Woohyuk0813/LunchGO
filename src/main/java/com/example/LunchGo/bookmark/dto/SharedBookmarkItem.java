package com.example.LunchGo.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SharedBookmarkItem {
    private Long restaurantId;
    private String name;
    private String roadAddress;
    private String detailAddress;
    private String imageUrl;
    private Double rating;
    private Long reviewCount;
}
