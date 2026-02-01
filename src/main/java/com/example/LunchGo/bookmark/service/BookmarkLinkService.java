package com.example.LunchGo.bookmark.service;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import com.example.LunchGo.bookmark.dto.BookmarkLinkListItem;
import com.example.LunchGo.bookmark.dto.BookmarkLinkRequest;
import com.example.LunchGo.bookmark.dto.BookmarkLinkRespondRequest;
import com.example.LunchGo.bookmark.dto.BookmarkLinkResponse;
import com.example.LunchGo.bookmark.dto.BookmarkLinkUserInfo;
import java.util.List;

public interface BookmarkLinkService {
    BookmarkLinkResponse requestLink(BookmarkLinkRequest request);
    void respondLink(Long linkId, BookmarkLinkRespondRequest request);
    List<BookmarkLinkListItem> getSentLinks(Long requesterId);
    List<BookmarkLinkListItem> getReceivedLinks(Long receiverId);
    List<BookmarkLinkListItem> getSentLinks(Long requesterId, BookmarkLinkStatus status);
    List<BookmarkLinkListItem> getReceivedLinks(Long receiverId, BookmarkLinkStatus status);
    BookmarkLinkUserInfo searchUserByEmail(String email);
    List<BookmarkLinkUserInfo> searchUsersByEmail(String query);
    void deleteLink(Long requesterId, Long receiverId);
}
