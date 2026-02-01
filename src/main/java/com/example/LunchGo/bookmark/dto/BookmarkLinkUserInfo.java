package com.example.LunchGo.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkLinkUserInfo {
    private Long userId;
    private String email;
    private String nickname;
    private String name;
    private String image;
}
