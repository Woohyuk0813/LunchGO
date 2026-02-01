package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.restaurant.dto.TrendingRestaurantItem;
import com.example.LunchGo.restaurant.service.RestaurantTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantTrendController {

    private final RestaurantTrendService restaurantTrendService;

    @GetMapping("/trending")
    public ResponseEntity<List<TrendingRestaurantItem>> getTrendingRestaurants(
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(restaurantTrendService.getTrendingRestaurants(days, limit));
    }
}
