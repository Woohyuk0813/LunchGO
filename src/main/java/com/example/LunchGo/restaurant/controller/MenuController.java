package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.service.MenuService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 특정 식당의 모든 메뉴 리스트를 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @return 메뉴 DTO 리스트를 포함하는 ResponseEntity
     */
    @GetMapping("/restaurants/{restaurantId}/menus")
    public ResponseEntity<List<MenuDTO>> getMenus(@PathVariable Long restaurantId) {
        List<MenuDTO> menus = menuService.getMenusByRestaurant(restaurantId);
        return ResponseEntity.ok(menus);
    }

    /**
     * 특정 식당의 단일 메뉴 정보를 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 메뉴 DTO
     */
    @GetMapping("/restaurants/{restaurantId}/menus/{menuId}")
    public ResponseEntity<MenuDTO> getMenu(
            @PathVariable Long restaurantId,
            @PathVariable Long menuId
    ) {
        MenuDTO menu = menuService.getMenuByRestaurant(restaurantId, menuId);
        return ResponseEntity.ok(menu);
    }

    /**
     * 특정 식당의 메뉴 이미지 목록을 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 이미지 URL 리스트
     */
    @GetMapping("/restaurants/{restaurantId}/menus/{menuId}/images")
    public ResponseEntity<List<String>> getMenuImages(
            @PathVariable Long restaurantId,
            @PathVariable Long menuId
    ) {
        List<String> images = menuService.getMenuImages(restaurantId, menuId);
        return ResponseEntity.ok(images);
    }

    /**
     * 특정 식당의 메뉴를 등록합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuDto      등록할 메뉴 정보
     * @return 생성된 메뉴 DTO
     */
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PostMapping("/business/restaurants/{restaurantId}/menus")
    public ResponseEntity<MenuDTO> createMenu(
            @PathVariable Long restaurantId,
            @RequestBody MenuDTO menuDto
    ) {
        MenuDTO createdMenu = menuService.createMenu(restaurantId, menuDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
    }

    /**
     * 특정 식당의 메뉴를 수정합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @param menuDto      수정할 메뉴 정보
     * @return 수정된 메뉴 DTO
     */
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PutMapping("/business/restaurants/{restaurantId}/menus/{menuId}")
    public ResponseEntity<MenuDTO> updateMenu(
            @PathVariable Long restaurantId,
            @PathVariable Long menuId,
            @RequestBody MenuDTO menuDto
    ) {
        MenuDTO updatedMenu = menuService.updateMenu(restaurantId, menuId, menuDto);
        return ResponseEntity.ok(updatedMenu);
    }

    /**
     * 특정 식당의 메뉴를 삭제합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 삭제 결과 ResponseEntity
     */
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @DeleteMapping("/business/restaurants/{restaurantId}/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long restaurantId,
            @PathVariable Long menuId
    ) {
        menuService.deleteMenu(restaurantId, menuId);
        return ResponseEntity.noContent().build();
    }
}
