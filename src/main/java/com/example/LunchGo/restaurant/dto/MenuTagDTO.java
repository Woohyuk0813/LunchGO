package com.example.LunchGo.restaurant.dto;

import com.example.LunchGo.tag.domain.TagCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuTagDTO {
    private Long tagId;
    private String content;
    private TagCategory category;
}
