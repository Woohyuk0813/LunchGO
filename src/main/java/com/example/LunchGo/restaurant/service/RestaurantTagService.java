package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.RestaurantTagDTO;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.tag.entity.SearchTag;
import com.example.LunchGo.tag.repository.SearchTagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RestaurantTagService {

    private final RestaurantRepository restaurantRepository;
    private final SearchTagRepository searchTagRepository;
    private final ModelMapper modelMapper;

    /**
     * 특정 식당에 연결된 태그 리스트를 조회합니다.
     * @param restaurantId 조회할 식당 ID
     * @return 식당 태그 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<RestaurantTagDTO> getTagsByRestaurant(Long restaurantId) {
        List<Long> restaurantTagIds = restaurantRepository.findTagIdsByRestaurantId(restaurantId);
        Map<Long, SearchTag> allRestaurantTagsMap = searchTagRepository.findAllById(restaurantTagIds)
                .stream()
                .collect(Collectors.toMap(SearchTag::getTagId, tag -> tag));

        return restaurantTagIds.stream()
                .map(tagId -> {
                    SearchTag tag = allRestaurantTagsMap.get(tagId);
                    return tag != null ? modelMapper.map(tag, RestaurantTagDTO.class) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 특정 식당에 태그 매핑을 생성합니다.
     * @param restaurantId 매핑을 생성할 식당 ID
     * @param selectedTagIds 선택된 태그 ID 리스트
     */
    public void createTagMappingsForRestaurant(Long restaurantId, List<Long> selectedTagIds) {
        if (selectedTagIds == null || selectedTagIds.isEmpty()) {
            return;
        }
        selectedTagIds.forEach(tagId -> {
            restaurantRepository.saveRestaurantTagMapping(restaurantId, tagId);
        });
    }

    /**
     * 특정 식당의 태그 매핑을 업데이트합니다. (기존 매핑 전체 삭제 후 새로 추가)
     * @param restaurantId 매핑을 업데이트할 식당 ID
     * @param selectedTagIds 새로운 태그 ID 리스트
     */
    public void updateTagMappingsForRestaurant(Long restaurantId, List<Long> selectedTagIds) {
        restaurantRepository.deleteRestaurantTagMappingsByRestaurantId(restaurantId);
        createTagMappingsForRestaurant(restaurantId, selectedTagIds);
    }
}
