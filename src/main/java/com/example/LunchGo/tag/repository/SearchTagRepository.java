package com.example.LunchGo.tag.repository;

import com.example.LunchGo.tag.domain.TagCategory;
import com.example.LunchGo.tag.entity.SearchTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchTagRepository extends JpaRepository<SearchTag, Long> {

    List<SearchTag> findByCategoryIn(List<TagCategory> categories);

    List<SearchTag> findByContentInAndCategory(List<String> contents, TagCategory category);
}
