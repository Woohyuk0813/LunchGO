package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.AdminBlindDecisionRequest;
import com.example.LunchGo.review.dto.AdminBlindDecisionResponse;
import com.example.LunchGo.review.dto.AdminReviewHideRequest;
import com.example.LunchGo.review.dto.AdminReviewHideResponse;
import com.example.LunchGo.review.entity.Review;
import com.example.LunchGo.review.repository.ReviewRepository;
import com.example.LunchGo.review.repository.ReviewTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewAdminService {

    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final ReviewSummaryCache reviewSummaryCache;

    public AdminBlindDecisionResponse decideBlind(Long reviewId, AdminBlindDecisionRequest request) {
        if (reviewId == null) {
            throw new IllegalArgumentException("reviewId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getDecision() == null || request.getDecision().isBlank()) {
            throw new IllegalArgumentException("decision is required");
        }
        if (request.getTagId() == null) {
            throw new IllegalArgumentException("tagId is required");
        }
        if (!reviewTagRepository.existsById(request.getTagId())) {
            throw new IllegalArgumentException("tagId is invalid");
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return null;
        }
        if (!"BLIND_REQUEST".equals(review.getStatus())) {
            throw new IllegalStateException("review is not in BLIND_REQUEST status");
        }

        boolean approve = "APPROVE".equalsIgnoreCase(request.getDecision());
        review.decideBlind(approve);
        Review saved = reviewRepository.save(review);
        reviewSummaryCache.invalidateRestaurant(saved.getRestaurantId());
        return new AdminBlindDecisionResponse(saved.getReviewId(), saved.getStatus());
    }

    public AdminReviewHideResponse hideReview(Long reviewId, AdminReviewHideRequest request) {
        if (reviewId == null) {
            throw new IllegalArgumentException("reviewId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return null;
        }

        String status = review.getStatus();
        if (!"PUBLIC".equals(status) && !"BLINDED".equals(status) && !"BLIND_REQUEST".equals(status)) {
            throw new IllegalStateException("review status cannot be toggled");
        }

        if (request.isHidden()) {
            review.setStatus("BLINDED");
        } else if ("BLINDED".equals(status)) {
            review.setStatus("PUBLIC");
        }

        Review saved = reviewRepository.save(review);
        reviewSummaryCache.invalidateRestaurant(saved.getRestaurantId());
        return new AdminReviewHideResponse(saved.getReviewId(), saved.getStatus());
    }
}
