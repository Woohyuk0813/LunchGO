package com.example.LunchGo.bookmark.dto;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkLinkListItem {
    private Long linkId;
    private Long requesterId;
    private Long receiverId;
    private BookmarkLinkStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private Long counterpartId;
    private String counterpartEmail;
    private String counterpartNickname;
    private String counterpartName;
    private String counterpartImage;

}
