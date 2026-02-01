package com.example.LunchGo.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ReviewTagMapId implements Serializable {

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "tag_id")
    private Long tagId;

    public ReviewTagMapId(Long reviewId, Long tagId) {
        this.reviewId = reviewId;
        this.tagId = tagId;
    }
}
