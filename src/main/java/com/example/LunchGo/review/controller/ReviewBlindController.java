package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.dto.ReviewBlindRequest;
import com.example.LunchGo.review.dto.ReviewBlindResponse;
import com.example.LunchGo.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/restaurants/{restaurantId}/reviews/{reviewId}/blind-requests")
public class ReviewBlindController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewBlindResponse> requestBlind(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId,
        @RequestBody ReviewBlindRequest request
    ) {
        ReviewBlindResponse response = reviewService.requestReviewBlind(restaurantId, reviewId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
