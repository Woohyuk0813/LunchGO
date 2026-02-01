package com.example.LunchGo.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkInfo {
    private Long bookmarkId; //삭제 시 필요
    private Long restaurantId; //등록시 필요
    private Long userId; //해당 유저 정보 필수
}
