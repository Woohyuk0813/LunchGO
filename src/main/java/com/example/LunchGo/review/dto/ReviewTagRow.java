package com.example.LunchGo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewTagRow {
    private Long reviewId;
    private Long tagId;
    private String name;
}
