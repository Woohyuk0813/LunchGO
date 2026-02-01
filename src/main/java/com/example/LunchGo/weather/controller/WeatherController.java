package com.example.LunchGo.weather.controller;

import com.example.LunchGo.weather.dto.WeatherResponse;
import com.example.LunchGo.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(
        @RequestParam("lat") double lat,
        @RequestParam("lon") double lon
    ) {
        return weatherService.fetchCurrentWeather(lat, lon);
    }
}
