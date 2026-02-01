package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.restaurant.dto.MenuImageRequest;
import com.example.LunchGo.restaurant.dto.MenuImageResponse;
import com.example.LunchGo.restaurant.service.BusinessMenuImageService;
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
@RequestMapping("/api/business/restaurants/{restaurantId}/menus/{menuId}/images")
public class BusinessMenuImageController {

    private final BusinessMenuImageService businessMenuImageService;

    @GetMapping
    public ResponseEntity<List<MenuImageResponse>> getImages(
        @PathVariable Long restaurantId,
        @PathVariable Long menuId
    ) {
        List<MenuImageResponse> response = businessMenuImageService.getImages(restaurantId, menuId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PostMapping
    public ResponseEntity<MenuImageResponse> createImage(
        @PathVariable Long restaurantId,
        @PathVariable Long menuId,
        @RequestBody MenuImageRequest request
    ) {
        MenuImageResponse response = businessMenuImageService.createImage(restaurantId, menuId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PutMapping("/{menuImageId}")
    public ResponseEntity<MenuImageResponse> updateImage(
        @PathVariable Long restaurantId,
        @PathVariable Long menuId,
        @PathVariable Long menuImageId,
        @RequestBody MenuImageRequest request
    ) {
        MenuImageResponse response = businessMenuImageService.updateImage(restaurantId, menuId, menuImageId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @DeleteMapping("/{menuImageId}")
    public ResponseEntity<Void> deleteImage(
        @PathVariable Long restaurantId,
        @PathVariable Long menuId,
        @PathVariable Long menuImageId
    ) {
        boolean deleted = businessMenuImageService.deleteImage(restaurantId, menuId, menuImageId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
