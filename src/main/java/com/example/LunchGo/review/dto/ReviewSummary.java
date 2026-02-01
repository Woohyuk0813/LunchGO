package com.example.LunchGo.review.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummary {
    private double avgRating;
    private long reviewCount;
    private List<TagCount> topTags;
}
