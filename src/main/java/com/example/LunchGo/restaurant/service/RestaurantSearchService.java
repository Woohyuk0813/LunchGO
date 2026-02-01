package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.RestaurantSearchParameter;
import com.example.LunchGo.restaurant.mapper.RestaurantSearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantSearchService {

    private final RestaurantSearchMapper restaurantSearchMapper;

    /**
     * 검색 파라미터를 기반으로 예약 가능한 식당 ID 목록을 조회합니다.
     * @param params 검색 파라미터 (날짜, 시간, 인원수 등)
     * @return 검색 조건에 맞는 식당 ID 목록
     */
    public List<Long> searchRestaurants(RestaurantSearchParameter params) {
        // 1. MyBatis Mapper를 호출하여 조건에 맞는 식당 ID 목록을 조회합니다.
        List<Long> restaurantIds = restaurantSearchMapper.findAvailableRestaurantIds(params);

        // 2. 조회된 ID 목록이 없으면, 빈 리스트를 반환합니다.
        if (CollectionUtils.isEmpty(restaurantIds)) {
            return Collections.emptyList();
        }

        // 3. 조회된 식당 ID 목록을 반환합니다.
        return restaurantIds;
    }
}


