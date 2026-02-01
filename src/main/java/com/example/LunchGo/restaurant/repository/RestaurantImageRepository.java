package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.RestaurantImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {

    Optional<RestaurantImage> findByRestaurantImageIdAndRestaurant_RestaurantId(Long restaurantImageId, Long restaurantId);

    List<RestaurantImage> findAllByRestaurant_RestaurantId(Long restaurantId);
}
