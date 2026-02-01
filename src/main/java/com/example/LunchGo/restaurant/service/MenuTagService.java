package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.MenuTagDTO;
import com.example.LunchGo.restaurant.dto.MenuTagMappingDTO;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import com.example.LunchGo.tag.entity.SearchTag;
import com.example.LunchGo.tag.repository.SearchTagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuTagService {

    private final MenuRepository menuRepository;
    private final SearchTagRepository searchTagRepository;
    private final ModelMapper modelMapper;

    /**
     * 특정 메뉴의 태그를 업데이트합니다. (기존 태그 모두 삭제 후 새로 추가)
     *
     * @param menuId  태그를 업데이트할 메뉴 ID
     * @param tagDtos 새로운 태그 DTO 리스트
     */
    @Transactional
    public void updateTagsForMenu(Long menuId, List<MenuTagDTO> tagDtos) {
        menuRepository.deleteMenuTagMappingsByMenuId(menuId);
        if (tagDtos != null && !tagDtos.isEmpty()) {
            for (MenuTagDTO tagDto : tagDtos) {
                if (tagDto.getTagId() != null) {
                    menuRepository.saveMenuTagMapping(menuId, tagDto.getTagId());
                }
            }
        }
    }

    /**
     * 특정 메뉴에 연결된 모든 태그를 조회합니다.
     *
     * @param menuId 조회할 메뉴 ID
     * @return 태그 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MenuTagDTO> getTagsForMenu(Long menuId) {
        List<MenuTagMappingDTO> mappings = menuRepository.findTagsForMenus(List.of(menuId));
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = mappings.stream().map(MenuTagMappingDTO::getTagId).distinct().collect(Collectors.toList());
        List<SearchTag> tags = searchTagRepository.findAllById(tagIds);

        return tags.stream()
                .map(tag -> modelMapper.map(tag, MenuTagDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 여러 메뉴에 대한 태그들을 한 번에 조회하여 N+1 문제를 해결합니다.
     *
     * @param menuIds 조회할 메뉴 ID 리스트
     * @return 메뉴 ID를 키로, 태그 DTO 리스트를 값으로 갖는 Map
     */
    @Transactional(readOnly = true)
    public Map<Long, List<MenuTagDTO>> getTagsForMenus(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<MenuTagMappingDTO> mappings = menuRepository.findTagsForMenus(menuIds);
        if (mappings.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> tagIds = mappings.stream().map(MenuTagMappingDTO::getTagId).distinct().collect(Collectors.toList());
        Map<Long, SearchTag> tagMap = searchTagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(SearchTag::getTagId, tag -> tag));

        return mappings.stream()
                .collect(Collectors.groupingBy(
                        MenuTagMappingDTO::getMenuId,
                        Collectors.mapping(
                                mapping -> {
                                    SearchTag tag = tagMap.get(mapping.getTagId());
                                    return tag != null ? modelMapper.map(tag, MenuTagDTO.class) : null;
                                },
                                Collectors.filtering(Objects::nonNull, Collectors.toList())
                        )
                ));
    }
}
