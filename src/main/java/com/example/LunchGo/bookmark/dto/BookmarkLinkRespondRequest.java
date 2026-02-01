package com.example.LunchGo.bookmark.dto;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkLinkRespondRequest {
    private BookmarkLinkStatus status;
}
