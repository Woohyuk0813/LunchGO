package com.example.LunchGo.tag.service;

import com.example.LunchGo.tag.domain.TagCategory;
import com.example.LunchGo.tag.dto.SearchTagDTO;
import com.example.LunchGo.tag.entity.SearchTag;
import com.example.LunchGo.tag.repository.SearchTagRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchTagService {

    private final SearchTagRepository searchTagRepository;

    public Map<String, List<SearchTagDTO>> getGroupedTags(List<String> filterCategories) {
        List<SearchTag> allTags;
        if (filterCategories == null || filterCategories.isEmpty()) {
            allTags = searchTagRepository.findAll();
        } else {
            List<TagCategory> categories = filterCategories.stream()
                .map(TagCategory::valueOf)
                .collect(Collectors.toList());
            allTags = searchTagRepository.findByCategoryIn(categories);
        }

        Map<TagCategory, List<SearchTag>> groupedByEntity = allTags.stream()
                .collect(Collectors.groupingBy(SearchTag::getCategory));

        Map<String, List<SearchTagDTO>> groupedTags = new HashMap<>();
        groupedByEntity.forEach((category, tags) -> {
            List<SearchTagDTO> tagDTOs = tags.stream().map(SearchTag::to).toList();
            groupedTags.put(category.name(), tagDTOs);
        });
        return groupedTags;
    }
}
