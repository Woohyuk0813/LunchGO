package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.MenuImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuImageRepository extends JpaRepository<MenuImage, Long> {

    Optional<MenuImage> findByMenuImageIdAndMenu_MenuIdAndMenu_RestaurantId(Long menuImageId, Long menuId, Long restaurantId);

    List<MenuImage> findAllByMenu_MenuIdAndMenu_RestaurantId(Long menuId, Long restaurantId);
}
