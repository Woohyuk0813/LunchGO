package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.domain.MenuCategory;
import com.example.LunchGo.restaurant.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.example.LunchGo.restaurant.dto.MenuTagMappingDTO; // MenuTagMappingDto import 예정

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByRestaurantIdAndIsDeletedFalse(Long restaurantId);

    Optional<Menu> findByMenuIdAndRestaurantIdAndIsDeletedFalse(Long menuId, Long restaurantId);

    List<Menu> findAllByMenuIdInAndRestaurantIdAndIsDeletedFalse(List<Long> menuIds, Long restaurantId);

    // 참고: `updateMenu` 메서드는 제거되었습니다. 이제 메뉴 업데이트는 `MenuService.updateMenusForRestaurant`에서
    // JPA의 변경 감지(dirty checking) 메커니즘을 통해 처리됩니다. `isDeleted = false` 조건 검사는
    // `findAllByRestaurantIdAndIsDeletedFalse`를 통한 초기 조회 시 암묵적으로 처리됩니다.

    @Modifying
    @Query("""
            UPDATE Menu m
               SET m.isDeleted = true
             WHERE m.menuId = :menuId
               AND m.restaurantId = :restaurantId
               AND m.isDeleted = false
            """)
    int softDeleteMenu(@Param("restaurantId") Long restaurantId,
                       @Param("menuId") Long menuId);

    // N+1 문제 해결을 위한 조회 메서드 (DTO 클래스 반환)
    @Query(value = "SELECT menu_id as menuId, tag_id as tagId FROM menu_tag_maps WHERE menu_id IN (:menuIds)", nativeQuery = true)
    List<MenuTagMappingDTO> findTagsForMenus(@Param("menuIds") List<Long> menuIds);

    @Modifying
    @Query(value = "INSERT INTO menu_tag_maps (menu_id, tag_id) VALUES (:menuId, :tagId)", nativeQuery = true)
    void saveMenuTagMapping(@Param("menuId") Long menuId, @Param("tagId") Long tagId);

    @Modifying
    @Query(value = "DELETE FROM menu_tag_maps WHERE menu_id = :menuId", nativeQuery = true)
    void deleteMenuTagMappingsByMenuId(@Param("menuId") Long menuId);

    @Query(value = "SELECT DISTINCT restaurant_id FROM menus WHERE name LIKE %:keyword%", nativeQuery = true)
    List<Long> findRestaurantIdsByMenuNameContaining(@Param("keyword") String keyword);

    @Query(value = "SELECT DISTINCT m.restaurant_id FROM menus m JOIN menu_tag_maps mt ON m.menu_id = mt.menu_id WHERE mt.tag_id IN (:tagIds)", nativeQuery = true)
    List<Long> findRestaurantIdsByMenuTagIds(@Param("tagIds") List<Long> tagIds);
    @Query(value = "SELECT image_url FROM menu_images WHERE menu_id = :menuId", nativeQuery = true)
    List<String> findMenuImageUrls(@Param("menuId") Long menuId);

    @Query(value = """
            SELECT mi.menu_id, mi.image_url
              FROM menu_images mi
              JOIN (
                    SELECT menu_id, MIN(menu_image_id) AS min_id
                      FROM menu_images
                     WHERE menu_id IN (:menuIds)
                     GROUP BY menu_id
                   ) first_images
                ON mi.menu_id = first_images.menu_id
               AND mi.menu_image_id = first_images.min_id
            """, nativeQuery = true)
    List<Object[]> findFirstMenuImageUrls(@Param("menuIds") List<Long> menuIds);
}
