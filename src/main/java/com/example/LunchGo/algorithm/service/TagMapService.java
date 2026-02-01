package com.example.LunchGo.algorithm.service;

import com.example.LunchGo.algorithm.dto.TagMapDTO;

import java.util.List;

public interface TagMapService {
    List<TagMapDTO> getTagSimilarityByUser(Long userId);
}
