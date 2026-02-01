package com.example.LunchGo.review.dto;

import com.example.LunchGo.review.domain.ReviewSort;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewListQuery {
    private Long restaurantId;
    private int size;
    private int offset;
    private ReviewSort sort;
    private List<Long> tagIds;
    private boolean includeBlinded;
}
