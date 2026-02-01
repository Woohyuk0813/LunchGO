package com.example.LunchGo.review.exception;

public class ReviewDuplicateException extends RuntimeException {
    private final Long reviewId;

    public ReviewDuplicateException(Long reviewId) {
        super("Duplicate review for reservation");
        this.reviewId = reviewId;
    }

    public ReviewDuplicateException(Long reviewId, Throwable cause) {
        super("Duplicate review for reservation", cause);
        this.reviewId = reviewId;
    }

    public Long getReviewId() {
        return reviewId;
    }
}
