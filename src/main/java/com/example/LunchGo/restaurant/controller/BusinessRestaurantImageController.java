package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.restaurant.dto.RestaurantImageRequest;
import com.example.LunchGo.restaurant.dto.RestaurantImageResponse;
import com.example.LunchGo.restaurant.service.BusinessRestaurantImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business/restaurants/{restaurantId}/images")
public class BusinessRestaurantImageController {

    private final BusinessRestaurantImageService businessRestaurantImageService;

    @GetMapping
    public ResponseEntity<List<RestaurantImageResponse>> getImages(@PathVariable Long restaurantId) {
        List<RestaurantImageResponse> response = businessRestaurantImageService.getImages(restaurantId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PostMapping
    public ResponseEntity<RestaurantImageResponse> createImage(
            @PathVariable Long restaurantId,
            @RequestBody RestaurantImageRequest request
    ) {
        RestaurantImageResponse response = businessRestaurantImageService.createImage(restaurantId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PutMapping("/{restaurantImageId}")
    public ResponseEntity<RestaurantImageResponse> updateImage(
            @PathVariable Long restaurantId,
            @PathVariable Long restaurantImageId,
            @RequestBody RestaurantImageRequest request
    ) {
        RestaurantImageResponse response = businessRestaurantImageService.updateImage(restaurantId, restaurantImageId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @DeleteMapping("/{restaurantImageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long restaurantId,
            @PathVariable Long restaurantImageId
    ) {
        boolean deleted = businessRestaurantImageService.deleteImage(restaurantId, restaurantImageId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}