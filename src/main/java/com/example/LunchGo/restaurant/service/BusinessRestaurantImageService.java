package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.RestaurantImageRequest;
import com.example.LunchGo.restaurant.dto.RestaurantImageResponse;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.entity.RestaurantImage;
import com.example.LunchGo.restaurant.repository.RestaurantImageRepository;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessRestaurantImageService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    public List<RestaurantImageResponse> getImages(Long restaurantId) {
        return restaurantImageRepository.findAllByRestaurant_RestaurantId(restaurantId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public RestaurantImageResponse createImage(Long restaurantId, RestaurantImageRequest request) {
        Optional<Restaurant> restaurant = restaurantRepository.findById(restaurantId);
        if (restaurant.isEmpty()) {
            return null;
        }
        RestaurantImage image = RestaurantImage.builder()
            .restaurant(restaurant.get())
            .imageUrl(request.getImageUrl())
            .build();
        RestaurantImage saved = restaurantImageRepository.save(image);
        return toResponse(saved);
    }

    @Transactional
    public RestaurantImageResponse updateImage(Long restaurantId, Long restaurantImageId, RestaurantImageRequest request) {
        Optional<RestaurantImage> image = restaurantImageRepository
            .findByRestaurantImageIdAndRestaurant_RestaurantId(restaurantImageId, restaurantId);
        if (image.isEmpty()) {
            return null;
        }
        image.get().setImageUrl(request.getImageUrl());
        return toResponse(image.get());
    }

    @Transactional
    public boolean deleteImage(Long restaurantId, Long restaurantImageId) {
        Optional<RestaurantImage> image = restaurantImageRepository
            .findByRestaurantImageIdAndRestaurant_RestaurantId(restaurantImageId, restaurantId);
        if (image.isEmpty()) {
            return false;
        }
        restaurantImageRepository.delete(image.get());
        return true;
    }

    private RestaurantImageResponse toResponse(RestaurantImage image) {
        return RestaurantImageResponse.builder()
            .restaurantImageId(image.getRestaurantImageId())
            .imageUrl(image.getImageUrl())
            .build();
    }

}
