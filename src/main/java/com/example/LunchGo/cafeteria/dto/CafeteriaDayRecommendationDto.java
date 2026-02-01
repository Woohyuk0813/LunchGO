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
public class CafeteriaDayRecommendationDto {
    private String day;
    private String date;
    private String avoidMenu;
    private List<CafeteriaRestaurantRecommendationDto> restaurants;
}
