package com.example.LunchGo.review.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewEditResponse {
    private Long reviewId;
    private Long restaurantId;
    private Long reservationId;
    private Long receiptId;
    private Integer rating;
    private String content;
    private List<TagResponse> tags;
    private List<String> images;
    private VisitInfo visitInfo;
}
