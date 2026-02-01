package com.example.LunchGo.review.service;

import com.example.LunchGo.review.domain.ReviewSort;
import com.example.LunchGo.review.dto.CreateReviewRequest;
import com.example.LunchGo.review.dto.CreateReviewResponse;
import com.example.LunchGo.review.dto.PageInfo;
import com.example.LunchGo.review.dto.ReceiptItemResponse;
import com.example.LunchGo.review.dto.RestaurantReviewListResponse;
import com.example.LunchGo.review.dto.ReviewEditResponse;
import com.example.LunchGo.review.dto.ReviewDetailResponse;
import com.example.LunchGo.review.dto.ReviewImageRow;
import com.example.LunchGo.review.dto.ReviewItemResponse;
import com.example.LunchGo.review.dto.ReviewListQuery;
import com.example.LunchGo.review.dto.ReviewSummary;
import com.example.LunchGo.review.dto.ReviewTagRow;
import com.example.LunchGo.review.dto.ReviewBlindRequest;
import com.example.LunchGo.review.dto.ReviewBlindResponse;
import com.example.LunchGo.review.dto.TagResponse;
import com.example.LunchGo.review.dto.UpdateReviewRequest;
import com.example.LunchGo.review.dto.UpdateReviewResponse;
import com.example.LunchGo.review.dto.VisitInfo;
import com.example.LunchGo.review.exception.ReviewDuplicateException;
import com.example.LunchGo.review.forbidden.ForbiddenWordService;
import com.example.LunchGo.image.service.ObjectStorageService;
import com.example.LunchGo.review.entity.Receipt;
import com.example.LunchGo.review.entity.ReceiptItem;
import com.example.LunchGo.review.entity.Review;
import com.example.LunchGo.review.entity.ReviewImage;
import com.example.LunchGo.review.entity.ReviewTagMap;
import com.example.LunchGo.review.mapper.ReviewReadMapper;
import com.example.LunchGo.review.repository.ReceiptItemRepository;
import com.example.LunchGo.review.repository.ReceiptRepository;
import com.example.LunchGo.review.repository.ReviewImageRepository;
import com.example.LunchGo.review.repository.ReviewRepository;
import com.example.LunchGo.review.repository.ReviewTagMapRepository;
import com.example.LunchGo.review.repository.ReviewTagRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewTagMapRepository reviewTagMapRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ReviewReadMapper reviewReadMapper;
    private final ObjectStorageService objectStorageService;
    private final ReviewTagRepository reviewTagRepository;
    private final ForbiddenWordService forbiddenWordService;
    private final ReviewSummaryCache reviewSummaryCache;

    @Override
    @Transactional
    public CreateReviewResponse createReview(Long restaurantId, CreateReviewRequest request) {
        validateCreateRequest(restaurantId, request);

        Long reservationId = request.getReservationId();
        if (reservationId != null) {
            Review existing = reviewRepository
                .findTopByReservationIdAndUserIdOrderByCreatedAtDesc(reservationId, request.getUserId())
                .orElse(null);
            if (existing != null && restaurantId.equals(existing.getRestaurantId())) {
                throw new ReviewDuplicateException(existing.getReviewId());
            }
        }

        Review review = new Review(
            restaurantId,
            request.getUserId(),
            request.getReceiptId(),
            request.getReservationId(),
            request.getRating(),
            request.getContent()
        );
        Review saved;
        try {
            saved = reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException ex) {
            if (reservationId != null) {
                Review existing = reviewRepository
                    .findTopByReservationIdAndUserIdOrderByCreatedAtDesc(reservationId, request.getUserId())
                    .orElse(null);
                if (existing != null && restaurantId.equals(existing.getRestaurantId())) {
                    throw new ReviewDuplicateException(existing.getReviewId(), ex);
                }
            }
            throw ex;
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ReviewTagMap> maps = new ArrayList<>();
            for (Long tagId : request.getTagIds()) {
                maps.add(new ReviewTagMap(saved.getReviewId(), tagId));
            }
            reviewTagMapRepository.saveAll(maps);
        }

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            int sortOrder = 0;
            for (String imageUrl : request.getImageUrls()) {
                images.add(new ReviewImage(saved.getReviewId(), imageUrl, sortOrder++));
            }
            reviewImageRepository.saveAll(images);
        }

        updateReceiptItems(
            request.getReceiptId(),
            mapCreateReceiptItems(request.getReceiptItems())
        );

        reviewSummaryCache.invalidateRestaurant(restaurantId);
        return new CreateReviewResponse(saved.getReviewId(), saved.getCreatedAt(), saved.getStatus());
    }

    @Override
    public RestaurantReviewListResponse getRestaurantReviews(Long restaurantId, int page, int size, ReviewSort sort, List<Long> tagIds) {
        return getRestaurantReviewsInternal(restaurantId, page, size, sort, tagIds, false);
    }

    @Override
    public ReviewDetailResponse getReviewDetail(Long restaurantId, Long reviewId) {
        return getReviewDetailInternal(restaurantId, reviewId, false);
    }

    @Override
    public RestaurantReviewListResponse getOwnerRestaurantReviews(Long restaurantId, int page, int size, ReviewSort sort, List<Long> tagIds) {
        return getRestaurantReviewsInternal(restaurantId, page, size, sort, tagIds, true);
    }

    @Override
    public ReviewDetailResponse getOwnerReviewDetail(Long restaurantId, Long reviewId) {
        return getReviewDetailInternal(restaurantId, reviewId, true);
    }

    private RestaurantReviewListResponse getRestaurantReviewsInternal(
        Long restaurantId,
        int page,
        int size,
        ReviewSort sort,
        List<Long> tagIds,
        boolean includeBlinded
    ) {
        int resolvedPage = Math.max(page, 1);
        int resolvedSize = Math.min(Math.max(size, 1), 50);
        int offset = (resolvedPage - 1) * resolvedSize;

        ReviewListQuery query = new ReviewListQuery(restaurantId, resolvedSize, offset, sort, tagIds, includeBlinded);

        ReviewSummary summary = getCachedReviewSummary(restaurantId, includeBlinded);

        List<ReviewItemResponse> items;
        items = reviewReadMapper.selectReviewItemsPage(query);
        if (items == null || items.isEmpty()) {
            items = Collections.emptyList();
        } else {
            List<Long> reviewIds = items.stream()
                .map(ReviewItemResponse::getReviewId)
                .filter(Objects::nonNull)
                .toList();

            Map<Long, List<TagResponse>> tagMap = new HashMap<>();
            List<ReviewTagRow> tagRows = reviewReadMapper.selectReviewTagsByReviewIds(reviewIds);
            if (tagRows != null) {
                for (ReviewTagRow row : tagRows) {
                    tagMap.computeIfAbsent(row.getReviewId(), key -> new ArrayList<>())
                        .add(new TagResponse(row.getTagId(), row.getName()));
                }
            }

            Map<Long, List<String>> imageMap = new HashMap<>();
            List<ReviewImageRow> imageRows = reviewReadMapper.selectReviewImagesByReviewIds(reviewIds);
            if (imageRows != null) {
                for (ReviewImageRow row : imageRows) {
                    imageMap.computeIfAbsent(row.getReviewId(), key -> new ArrayList<>())
                        .add(row.getImageUrl());
                }
            }

            for (ReviewItemResponse item : items) {
                Long reviewId = item.getReviewId();
                item.setTags(tagMap.getOrDefault(reviewId, Collections.emptyList()));
                item.setImages(imageMap.getOrDefault(reviewId, Collections.emptyList()));
                item.setContent(forbiddenWordService.maskForbiddenWords(item.getContent()));
            }
        }

        long total = reviewReadMapper.selectReviewCount(query);
        PageInfo pageInfo = new PageInfo(resolvedPage, resolvedSize, total);

        return new RestaurantReviewListResponse(summary, items, pageInfo);
    }

    private ReviewSummary getCachedReviewSummary(Long restaurantId, boolean includeBlinded) {
        ReviewSummary cached = reviewSummaryCache.get(restaurantId, includeBlinded);
        if (cached != null) {
            return cached;
        }

        ReviewSummary summary = reviewReadMapper.selectReviewSummary(restaurantId, includeBlinded);
        if (summary == null) {
            summary = new ReviewSummary(0.0, 0L, Collections.emptyList());
        } else {
            summary.setTopTags(reviewReadMapper.selectTopTags(restaurantId, includeBlinded));
        }

        reviewSummaryCache.put(restaurantId, includeBlinded, summary);
        return summary;
    }

    private ReviewDetailResponse getReviewDetailInternal(Long restaurantId, Long reviewId, boolean includeBlinded) {
        if (!reviewRepository.existsByReviewIdAndRestaurantId(reviewId, restaurantId)) {
            return null;
        }

        ReviewDetailResponse detail = reviewReadMapper.selectReviewDetail(reviewId, includeBlinded);
        if (detail == null) {
            return null;
        }

        detail.setTags(reviewReadMapper.selectReviewTags(reviewId));
        detail.setImages(reviewReadMapper.selectReviewImages(reviewId));
        detail.setComments(reviewReadMapper.selectReviewComments(reviewId));
        detail.setContent(forbiddenWordService.maskForbiddenWords(detail.getContent()));

        VisitInfo visitInfo = reviewReadMapper.selectVisitInfo(reviewId);
        if (visitInfo != null) {
            applyReceiptImagePresign(visitInfo);
            List<ReceiptItemResponse> items = reviewReadMapper.selectReceiptItemsByReviewId(reviewId);
            visitInfo.setMenuItems(items);
        }
        detail.setVisitInfo(visitInfo);

        return detail;
    }

    @Override
    public ReviewEditResponse getReviewEdit(Long restaurantId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return null;
        }

        ReviewEditResponse response = new ReviewEditResponse();
        response.setReviewId(reviewId);
        response.setRestaurantId(review.getRestaurantId());
        response.setReservationId(review.getReservationId());
        response.setReceiptId(review.getReceiptId());
        response.setRating(review.getRating());
        response.setContent(forbiddenWordService.maskForbiddenWords(review.getContent()));
        response.setTags(reviewReadMapper.selectReviewTags(reviewId));
        response.setImages(reviewReadMapper.selectReviewImages(reviewId));

        VisitInfo visitInfo = reviewReadMapper.selectVisitInfo(reviewId);
        if (visitInfo != null) {
            applyReceiptImagePresign(visitInfo);
            List<ReceiptItemResponse> items = reviewReadMapper.selectReceiptItemsByReviewId(reviewId);
            visitInfo.setMenuItems(items);
        }
        response.setVisitInfo(visitInfo);

        return response;
    }

    @Override
    @Transactional
    public UpdateReviewResponse updateReview(Long restaurantId, Long reviewId, UpdateReviewRequest request) {
        validateUpdateRequest(restaurantId, request);

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return null;
        }

        review.updateContent(request.getContent(), request.getRating());
        if (request.getReceiptId() != null) {
            review.updateReceiptId(request.getReceiptId());
        }

        reviewTagMapRepository.deleteByIdReviewId(reviewId);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ReviewTagMap> maps = new ArrayList<>();
            for (Long tagId : request.getTagIds()) {
                maps.add(new ReviewTagMap(reviewId, tagId));
            }
            reviewTagMapRepository.saveAll(maps);
        }

        reviewImageRepository.deleteByReviewId(reviewId);
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            int sortOrder = 0;
            for (String imageUrl : request.getImageUrls()) {
                images.add(new ReviewImage(reviewId, imageUrl, sortOrder++));
            }
            reviewImageRepository.saveAll(images);
        }

        updateReceiptItems(review.getReceiptId(), request.getReceiptItems());

        Review saved = reviewRepository.save(review);
        reviewSummaryCache.invalidateRestaurant(restaurantId);
        return new UpdateReviewResponse(saved.getReviewId(), saved.getUpdatedAt(), saved.getStatus());
    }

    @Override
    @Transactional
    public ReviewBlindResponse requestReviewBlind(Long restaurantId, Long reviewId, ReviewBlindRequest request) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getTagId() == null) {
            throw new IllegalArgumentException("tagId is required");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        if (!reviewTagRepository.existsById(request.getTagId())) {
            throw new IllegalArgumentException("tagId is invalid");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return null;
        }

        review.requestBlind(request.getTagId(), request.getReason().trim());
        Review saved = reviewRepository.save(review);
        reviewSummaryCache.invalidateRestaurant(restaurantId);
        return new ReviewBlindResponse(
            saved.getReviewId(),
            saved.getStatus(),
            saved.getBlindRequestTagId(),
            saved.getBlindRequestReason(),
            saved.getBlindRequestedAt()
        );
    }

    @Override
    @Transactional
    public boolean deleteReview(Long restaurantId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return false;
        }
        reviewRepository.delete(review);
        reviewSummaryCache.invalidateRestaurant(restaurantId);
        return true;
    }

    private void validateCreateRequest(Long restaurantId, CreateReviewRequest request) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    private void validateUpdateRequest(Long restaurantId, UpdateReviewRequest request) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    private void updateReceiptItems(Long receiptId, List<UpdateReviewRequest.ReceiptItemRequest> items) {
        if (receiptId == null || items == null) {
            return;
        }
        Receipt receipt = receiptRepository.findById(receiptId).orElse(null);
        if (receipt == null) {
            return;
        }

        receiptItemRepository.deleteByReceiptId(receiptId);

        List<ReceiptItem> entities = new ArrayList<>();
        int totalAmount = 0;
        for (UpdateReviewRequest.ReceiptItemRequest item : items) {
            if (item == null) {
                continue;
            }
            String name = item.getName() == null ? "" : item.getName().trim();
            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            int unitPrice = item.getPrice() == null ? 0 : item.getPrice();
            if (qty < 1) {
                qty = 1;
            }
            if (unitPrice < 0) {
                unitPrice = 0;
            }
            int lineAmount = unitPrice * qty;
            totalAmount += lineAmount;
            entities.add(new ReceiptItem(receiptId, name, qty, unitPrice, lineAmount));
        }

        if (!entities.isEmpty()) {
            receiptItemRepository.saveAll(entities);
        }
        receipt.updateConfirmedAmount(totalAmount);
        receiptRepository.save(receipt);
    }

    private List<UpdateReviewRequest.ReceiptItemRequest> mapCreateReceiptItems(
        List<CreateReviewRequest.ReceiptItemRequest> items
    ) {
        if (items == null) {
            return null;
        }
        List<UpdateReviewRequest.ReceiptItemRequest> mapped = new ArrayList<>();
        for (CreateReviewRequest.ReceiptItemRequest item : items) {
            if (item == null) {
                mapped.add(null);
                continue;
            }
            UpdateReviewRequest.ReceiptItemRequest request = new UpdateReviewRequest.ReceiptItemRequest();
            request.setName(item.getName());
            request.setQuantity(item.getQuantity());
            request.setPrice(item.getPrice());
            mapped.add(request);
        }
        return mapped;
    }

    private void applyReceiptImagePresign(VisitInfo visitInfo) {
        String storedValue = visitInfo.getReceiptImageUrl();
        if (storedValue == null || storedValue.isBlank()) {
            return;
        }
        String key = objectStorageService.normalizeKey(storedValue);
        if (key == null || !key.startsWith("receipts/")) {
            return;
        }
        String presigned = objectStorageService.createPresignedUrl(key, Duration.ofMinutes(5));
        visitInfo.setReceiptImageUrl(presigned);
    }
}
