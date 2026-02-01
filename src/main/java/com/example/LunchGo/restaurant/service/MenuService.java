package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.dto.MenuTagDTO;
import com.example.LunchGo.restaurant.entity.Menu;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuTagService menuTagService; // 식당메뉴-태그 매핑 관련 서비스 로직 처리
    private final ModelMapper modelMapper;

    /**
     * 특정 식당의 메뉴 리스트를 생성합니다.
     *
     * @param restaurantId 메뉴를 추가할 식당 ID
     * @param menuDtos     생성할 메뉴 정보 DTO 리스트
     */
    @Transactional
    public void createMenus(Long restaurantId, List<MenuDTO> menuDtos) {
        if (menuDtos == null || menuDtos.isEmpty()) {
            return;
        }
        for (MenuDTO menuDto : menuDtos) {
            Menu menu = Menu.builder()
                    .restaurantId(restaurantId)
                    .name(menuDto.getName())
                    .price(menuDto.getPrice())
                    .category(menuDto.getCategory())
                    .description(menuDto.getDescription() != null ? menuDto.getDescription() : "")
                    .build();
            Menu savedMenu = menuRepository.save(menu);

            // MenuTagService에 위임
            if (menuDto.getTags() != null && !menuDto.getTags().isEmpty()) {
                menuTagService.updateTagsForMenu(savedMenu.getMenuId(), menuDto.getTags());
            }
        }
    }

    /**
     * 특정 식당의 단일 메뉴를 생성합니다.
     *
     * @param restaurantId 메뉴를 추가할 식당 ID
     * @param menuDto      생성할 메뉴 정보 DTO
     * @return 생성된 메뉴 DTO
     */
    @Transactional
    public MenuDTO createMenu(Long restaurantId, MenuDTO menuDto) {
        Menu menu = Menu.builder()
                .restaurantId(restaurantId)
                .name(menuDto.getName())
                .price(menuDto.getPrice())
                .category(menuDto.getCategory())
                .description(menuDto.getDescription() != null ? menuDto.getDescription() : "")
                .build();
        Menu savedMenu = menuRepository.save(menu);
        // MenuTagService에 위임
        menuTagService.updateTagsForMenu(savedMenu.getMenuId(), menuDto.getTags());

        MenuDTO response = modelMapper.map(savedMenu, MenuDTO.class);
        response.setTags(menuDto.getTags() != null ? menuDto.getTags() : Collections.emptyList());
        response.setImageUrl(menuDto.getImageUrl());
        return response;
    }

    /**
     * 특정 식당의 단일 메뉴를 업데이트합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @param menuDto      업데이트할 메뉴 정보 DTO
     * @return 업데이트된 메뉴 DTO
     */
    @Transactional
    public MenuDTO updateMenu(Long restaurantId, Long menuId, MenuDTO menuDto) {
        Menu menu = menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + menuId));

        if (menuDto.getName() != null) {
            menu.setName(menuDto.getName());
        }
        if (menuDto.getCategory() != null) {
            menu.setCategory(menuDto.getCategory());
        }
        if (menuDto.getDescription() != null) {
            menu.setDescription(menuDto.getDescription());
        }
        if (menuDto.getPrice() != null) {
            menu.setPrice(menuDto.getPrice());
        }

        Menu savedMenu = menuRepository.save(menu);
        // MenuTagService에 위임
        menuTagService.updateTagsForMenu(savedMenu.getMenuId(), menuDto.getTags());

        MenuDTO response = modelMapper.map(savedMenu, MenuDTO.class);
        response.setTags(menuDto.getTags() != null ? menuDto.getTags() : Collections.emptyList());
        response.setImageUrl(menuDto.getImageUrl());
        return response;
    }

    /**
     * 특정 식당의 단일 메뉴를 삭제합니다. (소프트 삭제)
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     */
    @Transactional
    public void deleteMenu(Long restaurantId, Long menuId) {
        int updated = menuRepository.softDeleteMenu(restaurantId, menuId);
        if (updated == 0) {
            throw new NoSuchElementException("Menu not found with id: " + menuId);
        }
        // 태그 매핑도 함께 삭제
        menuTagService.updateTagsForMenu(menuId, Collections.emptyList());
    }

    /**
     * 특정 식당의 메뉴 리스트를 업데이트합니다. (변경분 비교 방식 + ModelMapper)
     *
     * @param restaurantId    업데이트할 식당 ID
     * @param updatedMenuDtos 업데이트할 메뉴 정보 DTO 리스트
     */
    @Transactional
    public void updateMenus(Long restaurantId, List<MenuDTO> updatedMenuDtos) {
        if (updatedMenuDtos == null) {
            updatedMenuDtos = new ArrayList<>(); // null이면 빈 리스트로 처리하여 모든 메뉴를 삭제
        }

        Map<Long, Menu> existingMenuMap = menuRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId)
                .stream()
                .collect(Collectors.toMap(Menu::getMenuId, menu -> menu));

        Set<Long> updatedMenuIds = updatedMenuDtos.stream()
                .map(MenuDTO::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (MenuDTO dto : updatedMenuDtos) {
            Long menuId = dto.getMenuId();
            boolean isNew = (menuId == null || menuId > 1000000L);

            if (isNew) {
                Menu newMenu = modelMapper.map(dto, Menu.class);
                newMenu.setRestaurantId(restaurantId);
                newMenu.setMenuId(null);
                Menu savedMenu = menuRepository.save(newMenu);
                // MenuTagService에 위임
                menuTagService.updateTagsForMenu(savedMenu.getMenuId(), dto.getTags());
            } else {
                Menu existingMenu = existingMenuMap.get(menuId);
                if (existingMenu != null) {
                    modelMapper.map(dto, existingMenu);
                    menuRepository.save(existingMenu);
                    // MenuTagService에 위임
                    menuTagService.updateTagsForMenu(existingMenu.getMenuId(), dto.getTags());
                }
            }
        }

        Set<Long> existingMenuIds = new HashSet<>(existingMenuMap.keySet());
        existingMenuIds.removeAll(updatedMenuIds);

        for (Long menuIdToDelete : existingMenuIds) {
            menuRepository.softDeleteMenu(restaurantId, menuIdToDelete);
            // MenuTagService를 통해 태그 매핑 삭제
            menuTagService.updateTagsForMenu(menuIdToDelete, Collections.emptyList());
        }
    }

    /**
     * 특정 식당의 메뉴 리스트를 조회합니다. (N+1 문제 해결 버전)
     *
     * @param restaurantId 조회할 식당 ID
     * @return 메뉴 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> getMenusByRestaurant(Long restaurantId) {
        List<Menu> menus = menuRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId);
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> menuIds = menus.stream().map(Menu::getMenuId).collect(Collectors.toList());
        // MenuTagService에서 태그 정보 조회
        Map<Long, List<MenuTagDTO>> tagsForMenus = menuTagService.getTagsForMenus(menuIds);
        Map<Long, String> imageUrlForMenus = new HashMap<>();
        for (Object[] row : menuRepository.findFirstMenuImageUrls(menuIds)) {
            if (row != null && row.length >= 2) {
                Long menuId = ((Number) row[0]).longValue();
                String imageUrl = (String) row[1];
                imageUrlForMenus.put(menuId, imageUrl);
            }
        }

        return menus.stream().map(menu -> {
            MenuDTO menuDto = modelMapper.map(menu, MenuDTO.class);
            menuDto.setTags(tagsForMenus.getOrDefault(menu.getMenuId(), Collections.emptyList()));
            menuDto.setImageUrl(imageUrlForMenus.get(menu.getMenuId()));
            return menuDto;
        }).collect(Collectors.toList());
    }

    /**
     * 특정 식당의 단일 메뉴 정보를 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 메뉴 DTO
     */
    @Transactional(readOnly = true)
    public MenuDTO getMenuByRestaurant(Long restaurantId, Long menuId) {
        Menu menu = menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + menuId));

        // MenuTagService에서 태그 정보 조회
        List<MenuTagDTO> menuTagDtos = menuTagService.getTagsForMenu(menuId);
        List<String> imageUrls = menuRepository.findMenuImageUrls(menuId);

        MenuDTO menuDto = modelMapper.map(menu, MenuDTO.class);
        menuDto.setTags(menuTagDtos);
        if (!imageUrls.isEmpty()) {
            menuDto.setImageUrl(imageUrls.get(0));
        }
        return menuDto;
    }

    /**
     * 특정 식당의 메뉴 이미지 목록을 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 이미지 URL 리스트
     */
    @Transactional(readOnly = true)
    public List<String> getMenuImages(Long restaurantId, Long menuId) {
        menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + menuId));

        return menuRepository.findMenuImageUrls(menuId);
    }
}
