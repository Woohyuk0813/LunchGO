package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.RestaurantSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantSummaryRepository extends JpaRepository<RestaurantSummary, Long> {
}
