package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.dto.CommentCreateRequest;
import com.example.LunchGo.review.dto.CommentResponse;
import com.example.LunchGo.review.service.ReviewCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/restaurants/{restaurantId}/reviews/{reviewId}/comments")
public class ReviewCommentController {

    private final ReviewCommentService reviewCommentService;

    @PostMapping
    public ResponseEntity<CommentResponse> create(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId,
        @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = reviewCommentService.createComment(restaurantId, reviewId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long restaurantId,
        @PathVariable Long reviewId,
        @PathVariable Long commentId
    ) {
        boolean deleted = reviewCommentService.deleteComment(restaurantId, reviewId, commentId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
