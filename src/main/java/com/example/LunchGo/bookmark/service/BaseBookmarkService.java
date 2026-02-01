package com.example.LunchGo.bookmark.service;

import com.example.LunchGo.bookmark.dto.BookmarkInfo;
import com.example.LunchGo.bookmark.dto.BookmarkListItem;
import com.example.LunchGo.bookmark.dto.BookmarkVisibilityRequest;
import com.example.LunchGo.bookmark.dto.SharedBookmarkItem;
import com.example.LunchGo.bookmark.entity.Bookmark;
import com.example.LunchGo.bookmark.repository.BookmarkListRow;
import com.example.LunchGo.bookmark.repository.SharedBookmarkRow;
import com.example.LunchGo.bookmark.repository.BookmarkLinkRepository;
import com.example.LunchGo.bookmark.repository.BookmarkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Log4j2
public class BaseBookmarkService implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkLinkRepository bookmarkLinkRepository;

    @Override
    public void save(BookmarkInfo bookmarkInfo) {
        //존재하는 즐겨찾기인지 확인
        if(bookmarkRepository.existsByUserIdAndRestaurantId(bookmarkInfo.getUserId(), bookmarkInfo.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 즐겨찾기에 등록되어 있습니다.");
        }

        bookmarkRepository.save(Bookmark.builder()
                .userId(bookmarkInfo.getUserId()) //해당 사용자
                .restaurantId(bookmarkInfo.getRestaurantId()) //식당Id를 즐겨찾기에 등록
                .promotionAgree(false) //기본값 false
                .isPublic(false) //기본값 비공개
                .build());
    }

    @Override
    @Transactional
    public void delete(BookmarkInfo bookmarkInfo) {
        if(!bookmarkRepository.existsByUserIdAndRestaurantId(bookmarkInfo.getUserId(), bookmarkInfo.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기에 등록되어있지 않습니다.");
        }

        bookmarkRepository.deleteByRestaurantIdAndUserId(bookmarkInfo.getRestaurantId(), bookmarkInfo.getUserId());
    }

    @Override
    @Transactional
    public void updateVisibility(BookmarkVisibilityRequest request) {
        if (request == null || request.getUserId() == null || request.getRestaurantId() == null || request.getIsPublic() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "즐겨찾기 공개 정보가 필요합니다.");
        }

        Bookmark bookmark = bookmarkRepository.findTopByUserIdAndRestaurantIdOrderByBookmarkIdDesc(
                request.getUserId(), request.getRestaurantId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기를 찾을 수 없습니다."));

        bookmark.updatePublic(request.getIsPublic());
    }

    @Override
    public List<SharedBookmarkItem> getSharedBookmarks(Long requesterId, Long targetUserId) {
        if (requesterId == null || targetUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청자/대상 사용자 정보가 필요합니다.");
        }

        if (!bookmarkLinkRepository.existsApprovedBetweenUsers(requesterId, targetUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "링크가 승인되지 않았습니다.");
        }

        List<SharedBookmarkRow> rows = bookmarkRepository.findPublicBookmarksWithRestaurant(targetUserId);
        return rows.stream()
            .map(row -> SharedBookmarkItem.builder()
                .restaurantId(row.getRestaurantId())
                .name(row.getName())
                .roadAddress(row.getRoadAddress())
                .detailAddress(row.getDetailAddress())
                .imageUrl(row.getImageUrl())
                .rating(row.getRating())
                .reviewCount(row.getReviewCount())
                .build())
            .toList();
    }

    @Override
    public List<BookmarkListItem> getBookmarks(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자 정보가 필요합니다.");
        }

        List<BookmarkListRow> rows = bookmarkRepository.findBookmarksByUserId(userId);
        return rows.stream()
            .map(row -> BookmarkListItem.builder()
                .bookmarkId(row.getBookmarkId())
                .restaurantId(row.getRestaurantId())
                .name(row.getName())
                .description(row.getDescription())
                .avgMainPrice(row.getAvgMainPrice())
                .reservationLimit(row.getReservationLimit())
                .promotionAgree(row.getPromotionAgree() != null && row.getPromotionAgree() == 1)
                .isPublic(row.getIsPublic() != null && row.getIsPublic() == 1)
                .imageUrl(row.getImageUrl())
                .rating(row.getRating())
                .reviewCount(row.getReviewCount())
                .build())
            .toList();
    }

    @Override
    @Transactional
    public void updatePromotionAgree(Long userId, Long restaurantId, Boolean promotionAgree) {
        if (userId == null || restaurantId == null || promotionAgree == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로모션 설정 정보가 필요합니다.");
        }

        Bookmark bookmark = bookmarkRepository.findTopByUserIdAndRestaurantIdOrderByBookmarkIdDesc(
                userId, restaurantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기를 찾을 수 없습니다."));

        bookmark.updatePromotionAgree(promotionAgree);
    }
}
