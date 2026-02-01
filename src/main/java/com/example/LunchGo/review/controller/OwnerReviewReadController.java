package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.domain.ReviewSort;
import com.example.LunchGo.review.dto.RestaurantReviewListResponse;
import com.example.LunchGo.review.dto.ReviewDetailResponse;
import com.example.LunchGo.review.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/restaurants/{restaurantId}/reviews")
public class OwnerReviewReadController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<RestaurantReviewListResponse> list(
        @PathVariable Long restaurantId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "RECOMMEND") ReviewSort sort,
        @RequestParam(required = false) List<Long> tagIds
    ) {
        RestaurantReviewListResponse response =
            reviewService.getOwnerRestaurantReviews(restaurantId, page, size, sort, tagIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> detail(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId
    ) {
        ReviewDetailResponse response = reviewService.getOwnerReviewDetail(restaurantId, reviewId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
