package com.example.LunchGo.bookmark.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "promotion_agree")
    private Boolean promotionAgree;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Builder
    public Bookmark(Long userId, Long restaurantId, Boolean promotionAgree, Boolean isPublic) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.promotionAgree = promotionAgree;
        this.isPublic = isPublic != null ? isPublic : false;
    }

    public void updatePublic(Boolean isPublic) {
        this.isPublic = isPublic != null && isPublic;
    }

    public void updatePromotionAgree(Boolean promotionAgree) {
        this.promotionAgree = promotionAgree != null && promotionAgree;
    }
}
