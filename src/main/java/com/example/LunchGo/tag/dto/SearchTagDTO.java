package com.example.LunchGo.tag.dto;

import com.example.LunchGo.tag.domain.TagCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchTagDTO {

    private Long tagId;
    private String content;
    private TagCategory category;
}
