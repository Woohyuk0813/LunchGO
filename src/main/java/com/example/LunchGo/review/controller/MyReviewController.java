package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.dto.MyReviewItem;
import com.example.LunchGo.review.service.MyReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class MyReviewController {

    private final MyReviewService myReviewService;

    @GetMapping("/my")
    public ResponseEntity<List<MyReviewItem>> myReviews(
        @RequestParam Long userId
    ) {
        List<MyReviewItem> response = myReviewService.getMyReviews(userId);
        return ResponseEntity.ok(response);
    }
}
