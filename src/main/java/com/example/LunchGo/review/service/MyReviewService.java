package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.MyReviewItem;
import java.util.List;

public interface MyReviewService {
    List<MyReviewItem> getMyReviews(Long userId);
}
