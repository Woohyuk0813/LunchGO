package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.CommentCreateRequest;
import com.example.LunchGo.review.dto.CommentResponse;

public interface ReviewCommentService {
    CommentResponse createComment(Long restaurantId, Long reviewId, CommentCreateRequest request);

    boolean deleteComment(Long restaurantId, Long reviewId, Long commentId);
}
