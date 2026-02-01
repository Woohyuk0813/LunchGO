package com.example.LunchGo.bookmark.controller;

import com.example.LunchGo.bookmark.domain.BookmarkLinkStatus;
import com.example.LunchGo.bookmark.dto.BookmarkLinkListItem;
import com.example.LunchGo.bookmark.dto.BookmarkLinkRequest;
import com.example.LunchGo.bookmark.dto.BookmarkLinkRespondRequest;
import com.example.LunchGo.bookmark.dto.BookmarkLinkResponse;
import com.example.LunchGo.bookmark.dto.BookmarkLinkUserInfo;
import com.example.LunchGo.bookmark.service.BookmarkLinkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark-links")
public class BookmarkLinkController {
    private final BookmarkLinkService bookmarkLinkService;

    @PostMapping
    public ResponseEntity<BookmarkLinkResponse> requestLink(@RequestBody BookmarkLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkLinkService.requestLink(request));
    }

    @PatchMapping("/{linkId}")
    public ResponseEntity<Void> respondLink(@PathVariable Long linkId,
                                            @RequestBody BookmarkLinkRespondRequest request) {
        bookmarkLinkService.respondLink(linkId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sent")
    public ResponseEntity<List<BookmarkLinkListItem>> getSentLinks(@RequestParam Long requesterId,
                                                                   @RequestParam(required = false) BookmarkLinkStatus status) {
        if (status == null) {
            return ResponseEntity.ok(bookmarkLinkService.getSentLinks(requesterId));
        }
        return ResponseEntity.ok(bookmarkLinkService.getSentLinks(requesterId, status));
    }

    @GetMapping("/received")
    public ResponseEntity<List<BookmarkLinkListItem>> getReceivedLinks(@RequestParam Long receiverId,
                                                                       @RequestParam(required = false) BookmarkLinkStatus status) {
        if (status == null) {
            return ResponseEntity.ok(bookmarkLinkService.getReceivedLinks(receiverId));
        }
        return ResponseEntity.ok(bookmarkLinkService.getReceivedLinks(receiverId, status));
    }

    @GetMapping("/search")
    public ResponseEntity<BookmarkLinkUserInfo> searchUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(bookmarkLinkService.searchUserByEmail(email));
    }

    @GetMapping("/search/list")
    public ResponseEntity<List<BookmarkLinkUserInfo>> searchUsersByEmail(@RequestParam String query) {
        return ResponseEntity.ok(bookmarkLinkService.searchUsersByEmail(query));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteLink(@RequestParam Long requesterId,
                                           @RequestParam Long receiverId) {
        bookmarkLinkService.deleteLink(requesterId, receiverId);
        return ResponseEntity.ok().build();
    }
}
