package com.example.LunchGo.review.service;

import com.example.LunchGo.review.domain.ReviewSort;
import com.example.LunchGo.review.dto.CreateReviewRequest;
import com.example.LunchGo.review.dto.CreateReviewResponse;
import com.example.LunchGo.review.dto.RestaurantReviewListResponse;
import com.example.LunchGo.review.dto.ReviewEditResponse;
import com.example.LunchGo.review.dto.ReviewDetailResponse;
import com.example.LunchGo.review.dto.UpdateReviewRequest;
import com.example.LunchGo.review.dto.UpdateReviewResponse;
import com.example.LunchGo.review.dto.ReviewBlindRequest;
import com.example.LunchGo.review.dto.ReviewBlindResponse;
import java.util.List;

public interface ReviewService {
    CreateReviewResponse createReview(Long restaurantId, CreateReviewRequest request);

    RestaurantReviewListResponse getRestaurantReviews(Long restaurantId, int page, int size, ReviewSort sort, List<Long> tagIds);

    ReviewDetailResponse getReviewDetail(Long restaurantId, Long reviewId);

    RestaurantReviewListResponse getOwnerRestaurantReviews(Long restaurantId, int page, int size, ReviewSort sort, List<Long> tagIds);

    ReviewDetailResponse getOwnerReviewDetail(Long restaurantId, Long reviewId);

    ReviewEditResponse getReviewEdit(Long restaurantId, Long reviewId);

    UpdateReviewResponse updateReview(Long restaurantId, Long reviewId, UpdateReviewRequest request);

    ReviewBlindResponse requestReviewBlind(Long restaurantId, Long reviewId, ReviewBlindRequest request);

    boolean deleteReview(Long restaurantId, Long reviewId);
}
