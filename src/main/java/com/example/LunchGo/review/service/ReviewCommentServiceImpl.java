package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.CommentCreateRequest;
import com.example.LunchGo.review.dto.CommentResponse;
import com.example.LunchGo.review.entity.Comment;
import com.example.LunchGo.review.mapper.ReviewReadMapper;
import com.example.LunchGo.review.repository.CommentRepository;
import com.example.LunchGo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewCommentServiceImpl implements ReviewCommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReadMapper reviewReadMapper;

    @Override
    @Transactional
    public CommentResponse createComment(Long restaurantId, Long reviewId, CommentCreateRequest request) {
        if (restaurantId == null || reviewId == null) {
            throw new IllegalArgumentException("restaurantId and reviewId are required");
        }
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("content is required");
        }
        if (!reviewRepository.existsByReviewIdAndRestaurantId(reviewId, restaurantId)) {
            return null;
        }

        Comment saved = commentRepository.save(new Comment(reviewId, request.getContent().trim(), "OWNER"));
        return reviewReadMapper.selectReviewCommentById(saved.getCommentId());
    }

    @Override
    @Transactional
    public boolean deleteComment(Long restaurantId, Long reviewId, Long commentId) {
        if (restaurantId == null || reviewId == null || commentId == null) {
            throw new IllegalArgumentException("restaurantId, reviewId, and commentId are required");
        }
        if (!reviewRepository.existsByReviewIdAndRestaurantId(reviewId, restaurantId)) {
            return false;
        }
        return commentRepository.findByCommentIdAndReviewId(commentId, reviewId)
            .map(comment -> {
                commentRepository.delete(comment);
                return true;
            })
            .orElse(false);
    }
}
