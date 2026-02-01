package com.example.LunchGo.bookmark.controller;

import com.example.LunchGo.bookmark.dto.BookmarkInfo;
import com.example.LunchGo.bookmark.dto.BookmarkListItem;
import com.example.LunchGo.bookmark.dto.BookmarkVisibilityRequest;
import com.example.LunchGo.bookmark.dto.SharedBookmarkItem;
import com.example.LunchGo.bookmark.service.BookmarkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/bookmark")
    public ResponseEntity<?> addBookmark(@RequestBody BookmarkInfo bookmarkInfo) {
        if(bookmarkInfo.getUserId() == null || bookmarkInfo.getRestaurantId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bookmarkService.save(bookmarkInfo); //이미 등록된 식당이면 409
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/bookmark")
    public ResponseEntity<?> deleteBookmark(@RequestBody BookmarkInfo bookmarkInfo) {
        if(bookmarkInfo.getUserId() == null || bookmarkInfo.getRestaurantId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bookmarkService.delete(bookmarkInfo); //등록되어 있지 않은 식당이면 404
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/bookmark/visibility")
    public ResponseEntity<?> updateBookmarkVisibility(@RequestBody BookmarkVisibilityRequest request) {
        if (request.getUserId() == null || request.getRestaurantId() == null || request.getIsPublic() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bookmarkService.updateVisibility(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/bookmark/shared")
    public ResponseEntity<List<SharedBookmarkItem>> getSharedBookmarks(@RequestParam Long requesterId,
                                                                       @RequestParam Long targetUserId) {
        return ResponseEntity.ok(bookmarkService.getSharedBookmarks(requesterId, targetUserId));
    }

    @GetMapping("/bookmark/list")
    public ResponseEntity<List<BookmarkListItem>> getBookmarks(@RequestParam Long userId) {
        return ResponseEntity.ok(bookmarkService.getBookmarks(userId));
    }

    @PatchMapping("/bookmark/promotion")
    public ResponseEntity<?> updateBookmarkPromotion(@RequestParam Long userId,
                                                     @RequestParam Long restaurantId,
                                                     @RequestParam Boolean promotionAgree) {
        bookmarkService.updatePromotionAgree(userId, restaurantId, promotionAgree);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
