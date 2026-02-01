package com.example.LunchGo.tag.entity;

import com.example.LunchGo.tag.domain.TagCategory;
import com.example.LunchGo.tag.dto.SearchTagDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "search_tags")
public class SearchTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "content", length = 50, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private TagCategory category;

    public SearchTagDTO to() {
        return new SearchTagDTO(tagId, content, category);
    }
}
