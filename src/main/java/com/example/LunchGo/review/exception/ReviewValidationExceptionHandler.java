package com.example.LunchGo.review.exception;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.LunchGo.review.controller.ReviewController;

@RestControllerAdvice(basePackageClasses = ReviewController.class)
public class ReviewValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = Optional.ofNullable(ex.getBindingResult().getFieldErrors())
            .flatMap(errors -> errors.stream().findFirst())
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("요청이 올바르지 않습니다.");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("message", "이미 등록된 예약 리뷰가 있습니다."));
    }

    @ExceptionHandler(ReviewDuplicateException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateReview(ReviewDuplicateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of(
                "message", "이미 등록된 예약 리뷰가 있습니다.",
                "reviewId", ex.getReviewId()
            ));
    }
}
