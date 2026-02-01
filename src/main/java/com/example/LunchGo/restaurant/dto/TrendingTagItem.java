package com.example.LunchGo.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrendingTagItem {
    private Long tagId;
    private String content;
    private Integer count;
}
