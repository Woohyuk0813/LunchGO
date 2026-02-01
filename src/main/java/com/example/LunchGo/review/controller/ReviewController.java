package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.domain.ReviewSort;
import com.example.LunchGo.review.dto.CreateReviewRequest;
import com.example.LunchGo.review.dto.CreateReviewResponse;
import com.example.LunchGo.review.dto.RestaurantReviewListResponse;
import com.example.LunchGo.review.dto.ReviewEditResponse;
import com.example.LunchGo.review.dto.ReviewDetailResponse;
import com.example.LunchGo.review.dto.UpdateReviewRequest;
import com.example.LunchGo.review.dto.UpdateReviewResponse;
import com.example.LunchGo.review.service.ReviewService;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants/{restaurantId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<CreateReviewResponse> create(
        @PathVariable Long restaurantId,
        @Valid @RequestBody CreateReviewRequest request
    ) {
        CreateReviewResponse response = reviewService.createReview(restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<RestaurantReviewListResponse> list(
        @PathVariable Long restaurantId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "RECOMMEND") ReviewSort sort,
        @RequestParam(required = false) List<Long> tagIds
    ) {
        RestaurantReviewListResponse response = reviewService.getRestaurantReviews(restaurantId, page, size, sort, tagIds);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> detail(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId
    ) {
        ReviewDetailResponse response = reviewService.getReviewDetail(restaurantId, reviewId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}/edit")
    public ResponseEntity<ReviewEditResponse> edit(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId
    ) {
        ReviewEditResponse response = reviewService.getReviewEdit(restaurantId, reviewId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<UpdateReviewResponse> update(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId,
        @Valid @RequestBody UpdateReviewRequest request
    ) {
        UpdateReviewResponse response = reviewService.updateReview(restaurantId, reviewId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId
    ) {
        boolean deleted = reviewService.deleteReview(restaurantId, reviewId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
