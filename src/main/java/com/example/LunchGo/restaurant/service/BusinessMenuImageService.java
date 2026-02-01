package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.MenuImageRequest;
import com.example.LunchGo.restaurant.dto.MenuImageResponse;
import com.example.LunchGo.restaurant.entity.Menu;
import com.example.LunchGo.restaurant.entity.MenuImage;
import com.example.LunchGo.restaurant.repository.MenuImageRepository;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessMenuImageService {

    private final MenuRepository menuRepository;
    private final MenuImageRepository menuImageRepository;

    public List<MenuImageResponse> getImages(Long restaurantId, Long menuId) {
        menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("Menu not found with id: " + menuId));
        return menuImageRepository.findAllByMenu_MenuIdAndMenu_RestaurantId(menuId, restaurantId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MenuImageResponse createImage(Long restaurantId, Long menuId, MenuImageRequest request) {
        Optional<Menu> menu = menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId);
        if (menu.isEmpty()) {
            return null;
        }
        MenuImage image = MenuImage.builder()
            .menu(menu.get())
            .imageUrl(request.getImageUrl())
            .build();
        MenuImage saved = menuImageRepository.save(image);
        return toResponse(saved);
    }

    @Transactional
    public MenuImageResponse updateImage(Long restaurantId, Long menuId, Long menuImageId, MenuImageRequest request) {
        Optional<MenuImage> image = menuImageRepository
            .findByMenuImageIdAndMenu_MenuIdAndMenu_RestaurantId(menuImageId, menuId, restaurantId);
        if (image.isEmpty()) {
            return null;
        }
        image.get().setImageUrl(request.getImageUrl());
        return toResponse(image.get());
    }

    @Transactional
    public boolean deleteImage(Long restaurantId, Long menuId, Long menuImageId) {
        Optional<MenuImage> image = menuImageRepository
            .findByMenuImageIdAndMenu_MenuIdAndMenu_RestaurantId(menuImageId, menuId, restaurantId);
        if (image.isEmpty()) {
            return false;
        }
        menuImageRepository.delete(image.get());
        return true;
    }

    private MenuImageResponse toResponse(MenuImage image) {
        return MenuImageResponse.builder()
            .menuImageId(image.getMenuImageId())
            .imageUrl(image.getImageUrl())
            .build();
    }

}
