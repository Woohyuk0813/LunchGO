package com.example.LunchGo.review.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantReviewListResponse {
    private ReviewSummary summary;
    private List<ReviewItemResponse> items;
    private PageInfo page;
}
