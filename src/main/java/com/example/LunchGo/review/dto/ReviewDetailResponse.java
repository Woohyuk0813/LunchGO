package com.example.LunchGo.review.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDetailResponse {
    private Long reviewId;
    private String author;
    private String company;
    private Integer visitCount;
    private Integer rating;
    private String content;
    private List<TagResponse> tags;
    private List<String> images;
    private LocalDate createdAt;
    private String status;
    private boolean isBlinded;
    private String blindReason;
    private VisitInfo visitInfo;
    private List<CommentResponse> comments;
    private Long blindRequestTagId;
    private String blindRequestTagName;
    private String blindRequestReason;
    private LocalDateTime blindRequestedAt;
}
