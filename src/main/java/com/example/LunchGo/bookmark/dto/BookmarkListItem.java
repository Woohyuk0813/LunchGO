package com.example.LunchGo.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkListItem {
    private Long bookmarkId;
    private Long restaurantId;
    private String name;
    private String description;
    private Integer avgMainPrice;
    private Integer reservationLimit;
    private Boolean promotionAgree;
    private Boolean isPublic;
    private String imageUrl;
    private Double rating;
    private Long reviewCount;
}
