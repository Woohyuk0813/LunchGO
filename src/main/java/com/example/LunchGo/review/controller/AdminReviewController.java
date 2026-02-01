package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.dto.AdminBlindDecisionRequest;
import com.example.LunchGo.review.dto.AdminBlindDecisionResponse;
import com.example.LunchGo.review.dto.AdminReviewHideRequest;
import com.example.LunchGo.review.dto.AdminReviewHideResponse;
import com.example.LunchGo.review.dto.ReviewAdminItemResponse;
import com.example.LunchGo.review.mapper.ReviewReadMapper;
import com.example.LunchGo.review.service.ReviewAdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewReadMapper reviewReadMapper;
    private final ReviewAdminService reviewAdminService;

    @GetMapping
    public ResponseEntity<List<ReviewAdminItemResponse>> list() {
        return ResponseEntity.ok(reviewReadMapper.selectAdminReviews());
    }

    @PatchMapping("/{reviewId}/blind-requests")
    public ResponseEntity<AdminBlindDecisionResponse> decideBlind(
        @PathVariable Long reviewId,
        @RequestBody AdminBlindDecisionRequest request
    ) {
        try {
            AdminBlindDecisionResponse response = reviewAdminService.decideBlind(reviewId, request);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{reviewId}/hide")
    public ResponseEntity<AdminReviewHideResponse> hideReview(
        @PathVariable Long reviewId,
        @RequestBody AdminReviewHideRequest request
    ) {
        try {
            AdminReviewHideResponse response = reviewAdminService.hideReview(reviewId, request);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
