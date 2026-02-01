package com.example.LunchGo.review.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "review_tag_maps")
public class ReviewTagMap {

    @EmbeddedId
    private ReviewTagMapId id;

    public ReviewTagMap(Long reviewId, Long tagId) {
        this.id = new ReviewTagMapId(reviewId, tagId);
    }
}
