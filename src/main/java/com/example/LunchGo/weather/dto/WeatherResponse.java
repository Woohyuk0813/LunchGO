package com.example.LunchGo.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeatherResponse {
    private Double temp;
    private Double feelsLike;
    private String condition;
    private String description;
}
