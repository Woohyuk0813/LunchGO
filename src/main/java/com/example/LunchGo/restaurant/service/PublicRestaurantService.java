package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.ImageDTO;
import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.dto.RegularHolidayDTO;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantTagDTO;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.entity.RestaurantSummary;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.stats.RestaurantStatsEventService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import com.example.LunchGo.restaurant.dto.RestaurantSummaryResponse;
import com.example.LunchGo.restaurant.repository.RestaurantSummaryRepository;
import com.example.LunchGo.map.service.KakaoGeoService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantSummaryRepository restaurantSummaryRepository;
    private final MenuService menuService;
    private final RegularHolidayService regularHolidayService;
    private final RestaurantTagService restaurantTagService;
    private final RestaurantStatsEventService statsEventService;
    private final KakaoGeoService kakaoGeoService;
    private final ModelMapper modelMapper;

    /**
     * 식당 전체 목록을 조회합니다. (캐싱 적용 - Cache Aside 패턴)
     * - @Cacheable: Redis에 'restaurantSummaries::all' 키로 데이터가 있으면 즉시 반환(DB 조회 생략).
     * - 데이터가 없으면(Cache Miss) DB 조회 및 Kakao API 로직 수행 후 결과를 Redis에 저장.
     * - unless = "#result == null": 만약 결과가 null이면(API 오류 등) 캐싱하지 않음.
     */
    @Cacheable(value = "restaurantSummaries", key = "'all'", unless = "#result == null")
    public List<RestaurantSummaryResponse> getRestaurantSummaries() {
        return fetchSummariesFromDb();
    }

    /**
     * 식당 전체 목록 캐시를 강제로 갱신합니다. (Cache Warming)
     * - @CachePut: 메서드 실행 결과를 무조건 캐시에 덮어씁니다. (기존 캐시 무시)
     * - 스케줄러(RestaurantCacheScheduler)에 의해 55분마다 호출되어, 
     *   사용자가 만료된 캐시로 인해 느린 응답을 겪는 것을 방지합니다.
     */
    @CachePut(value = "restaurantSummaries", key = "'all'", unless = "#result == null")
    public List<RestaurantSummaryResponse> refreshRestaurantSummaries() {
        log.debug("Refreshing restaurant summaries cache...");
        return fetchSummariesFromDb();
    }

    // 실제 비즈니스 로직 (DB 조회 + 좌표 변환) 추출
    private List<RestaurantSummaryResponse> fetchSummariesFromDb() {
        // [개선] 외부 API 호출(I/O-bound)이 포함되어 있으므로, 공용 ForkJoinPool 고갈 방지를 위해 
        // parallelStream() 대신 일반 stream()을 사용합니다. 캐시 워밍 시 성능보다 안정성을 우선합니다.
        return restaurantSummaryRepository.findAll().stream()
                .map(entity -> {
                    // 1. 엔티티를 DTO로 변환
                    RestaurantSummaryResponse response = modelMapper.map(entity, RestaurantSummaryResponse.class);
                    
                    // 2. 순수 도로명 주소로 좌표 조회
                    KakaoGeoService.GeoCoordinate coords = kakaoGeoService.getCoordinateByAddress(entity.getRoadAddress());
                    
                    if (coords != null) {
                        // 불변 객체이므로 toBuilder()를 사용하여 좌표가 설정된 새로운 객체 생성
                        return response.toBuilder()
                                .latitude(coords.latitude())
                                .longitude(coords.longitude())
                                .build();
                    }
                    return null; // 좌표 없으면 제외 대상
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId, String userKey) {
        // 1. Restaurant 기본 정보 및 이미지 조회 (Fetch Join 활용)
        Restaurant restaurant = restaurantRepository.findByIdWithImages(restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found with id: " + restaurantId));

        if (userKey != null && !userKey.isBlank()) {
            statsEventService.recordView(restaurantId, userKey);
        }

        // 2. 메뉴, 정기 휴무일, 태그 정보는 각 전문 서비스에 위임
        List<MenuDTO> menuDtos = menuService.getMenusByRestaurant(restaurantId);
        List<RegularHolidayDTO> regularHolidayDtos = regularHolidayService.getRegularHolidaysByRestaurant(restaurantId);
        List<RestaurantTagDTO> restaurantTagDtos = restaurantTagService.getTagsByRestaurant(restaurantId);

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
}
