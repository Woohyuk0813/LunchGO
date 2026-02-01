package com.example.LunchGo.restaurant.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class RestaurantSearchParameter {
    private LocalDate date;
    private LocalTime time;
    private Integer partySize;
    private List<String> menuTypes;
    private List<String> restaurantTags;
    private List<String> avoidIngredients;
    private Boolean preorderAvailable;
    // TODO: 향후 태그 검색 기능 추가 시, 여기에 필드 추가
}
