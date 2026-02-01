package com.example.LunchGo.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkVisibilityRequest {
    private Long userId;
    private Long restaurantId;
    private Boolean isPublic;
}
