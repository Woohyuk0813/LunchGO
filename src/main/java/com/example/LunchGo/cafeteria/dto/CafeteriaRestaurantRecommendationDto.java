package com.example.LunchGo.cafeteria.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaRestaurantRecommendationDto {
    private String id;
    private String name;
    private String address;
    private String price;
    private Double rating;
    private Long reviews;
    private String image;
    private List<CafeteriaTagCountDto> topTags;
}
