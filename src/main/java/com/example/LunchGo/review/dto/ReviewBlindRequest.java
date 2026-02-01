package com.example.LunchGo.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewBlindRequest {
    private Long tagId;
    private String reason;
}
