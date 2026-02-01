package com.example.LunchGo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageInfo {
    private int current;
    private int size;
    private long total;
}
