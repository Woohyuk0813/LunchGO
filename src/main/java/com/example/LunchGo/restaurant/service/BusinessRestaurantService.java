package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.domain.RestaurantStatus;
import com.example.LunchGo.restaurant.dto.ImageDTO;
import com.example.LunchGo.restaurant.dto.RegularHolidayDTO;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantCreateRequest;
import com.example.LunchGo.restaurant.dto.RestaurantTagDTO;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.dto.RestaurantUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessRestaurantService {

    private final RestaurantRepository restaurantRepository;

    private final MenuService menuService;
    private final RegularHolidayService regularHolidayService;
    private final RestaurantTagService restaurantTagService;

    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId, Long ownerId) {
        // 1. Restaurant 기본 정보 및 이미지 조회 (Fetch Join 활용) 및 소유권 검증
        Restaurant restaurant = restaurantRepository.findByRestaurantIdAndOwnerId(restaurantId, ownerId)
                .orElseThrow(() -> new AccessDeniedException("접근 권한이 없습니다."));

        // 2. 메뉴, 정기 휴무일, 태그 정보는 각 전문 서비스에 위임
        List<MenuDTO> menuDtos = menuService.getMenusByRestaurant(restaurantId);
        List<RegularHolidayDTO> regularHolidayDtos = regularHolidayService.getRegularHolidaysByRestaurant(restaurantId);
        List<RestaurantTagDTO> restaurantTagDtos = restaurantTagService.getTagsByRestaurant(restaurantId); // RestaurantTagService에 위임

        // 3. DTO 매핑
        List<ImageDTO> imageDtos = restaurant.getRestaurantImages().stream()
                .map(img -> ImageDTO.builder().imageUrl(img.getImageUrl()).build())
                .collect(Collectors.toList());

        RestaurantDetailResponse response = modelMapper.map(restaurant, RestaurantDetailResponse.class);
        response.setMenus(menuDtos);
        response.setImages(imageDtos);
        response.setTags(restaurantTagDtos);
        response.setRegularHolidays(regularHolidayDtos);
        return response;
    }


    @Transactional
    // 새 식당이 등록되면 전체 목록 캐시(restaurantSummaries)를 삭제하여, 다음 조회 시 최신 목록을 가져오도록 함.
    @CacheEvict(value = "restaurantSummaries", allEntries = true)
    public Long createRestaurant(Long ownerId, RestaurantCreateRequest request) {
        // 1. Restaurant 엔티티 생성 및 저장
        Restaurant restaurant = modelMapper.map(request, Restaurant.class);
        restaurant.setOwnerId(ownerId); // 실제 ownerId 사용
        restaurant.setStatus(RestaurantStatus.OPEN); // 초기 상태는 OPEN

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        Long newRestaurantId = savedRestaurant.getRestaurantId();

        // 2. 식당 메뉴 정보 저장은 MenuService에 위임
        menuService.createMenus(newRestaurantId, request.getMenus());
        
        // 3. 정기 휴무일 정보 저장은 RegularHolidayService에 위임
        regularHolidayService.createRegularHolidaysForRestaurant(newRestaurantId, request.getRegularHolidayNumbers());

        // 4. 태그 정보 저장은 RestaurantTagService에 위임
        restaurantTagService.createTagMappingsForRestaurant(newRestaurantId, request.getSelectedTagIds());

        return newRestaurantId;
    }

    @Transactional
    // 식당 정보(이름, 주소 등)가 수정되면 캐시를 삭제하여 최신 정보를 반영하도록 함.
    @CacheEvict(value = "restaurantSummaries", allEntries = true)
    public RestaurantDetailResponse updateRestaurant(Long id, Long ownerId, RestaurantUpdateRequest request) {
        // 1. 식당 정보 조회 및 소유권 검증
        Restaurant restaurant = restaurantRepository.findByRestaurantIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new AccessDeniedException("접근 권한이 없습니다."));

        // 2. 기본 정보 업데이트
        modelMapper.map(request, restaurant);

        // 3. 식당 메뉴 정보 업데이트는 MenuService에 위임
        menuService.updateMenus(id, request.getMenus());

        // 4. 정기 휴무일 업데이트는 RegularHolidayService에 위임
        regularHolidayService.updateRegularHolidaysForRestaurant(id, request.getRegularHolidayNumbers());

        // 5. 태그 정보 업데이트는 RestaurantTagService에 위임
        restaurantTagService.updateTagMappingsForRestaurant(id, request.getSelectedTagIds());

        // 6. 변경된 내용을 DB에 즉시 반영하기 위해 save 호출
        restaurantRepository.save(restaurant);

        // 7. 업데이트된 전체 정보 다시 조회하여 반환
        return getRestaurantDetail(id, ownerId);
    }

    public Optional<Long> findRestaurantIdByOwnerId(Long ownerId) {
        return restaurantRepository.findRestaurantIdByOwnerId(ownerId);
    }
}
