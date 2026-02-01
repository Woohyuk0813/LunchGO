package com.example.LunchGo.bookmark.repository;

import com.example.LunchGo.bookmark.dto.SharedBookmarkItem;
import com.example.LunchGo.bookmark.entity.Bookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    /**
     * 사용자 즐겨찾기 삭제
     * */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.restaurantId = :restaurantId AND b.userId = :userId")
    int deleteByRestaurantIdAndUserId(Long restaurantId, Long userId);

    /**
     * 즐겨찾기 여부 확인
     * */
    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    Optional<Bookmark> findTopByUserIdAndRestaurantIdOrderByBookmarkIdDesc(Long userId, Long restaurantId);

    @Query(value = """
        SELECT
            r.restaurant_id AS restaurantId,
            r.name AS name,
            r.road_address AS roadAddress,
            r.detail_address AS detailAddress,
            ri.image_url AS imageUrl,
            COALESCE(AVG(rv.rating), 0) AS rating,
            COUNT(rv.review_id) AS reviewCount
        FROM bookmarks b
        JOIN restaurants r ON b.restaurant_id = r.restaurant_id
        LEFT JOIN restaurant_images ri
          ON ri.restaurant_id = r.restaurant_id
         AND ri.restaurant_image_id = (
            SELECT MIN(ri2.restaurant_image_id)
            FROM restaurant_images ri2
            WHERE ri2.restaurant_id = r.restaurant_id
          )
        LEFT JOIN reviews rv ON rv.restaurant_id = r.restaurant_id
        WHERE b.user_id = :userId
          AND b.is_public = true
        GROUP BY r.restaurant_id, r.name, r.road_address, r.detail_address, ri.image_url
        ORDER BY MAX(b.bookmark_id) DESC
        """, nativeQuery = true)
    List<com.example.LunchGo.bookmark.repository.SharedBookmarkRow> findPublicBookmarksWithRestaurant(Long userId);

    @Query(value = """
        SELECT
            b.bookmark_id AS bookmarkId,
            r.restaurant_id AS restaurantId,
            r.name AS name,
            r.description AS description,
            r.avg_main_price AS avgMainPrice,
            r.reservation_limit AS reservationLimit,
            CASE WHEN b.promotion_agree = 1 THEN 1 ELSE 0 END AS promotionAgree,
            CASE WHEN b.is_public = 1 THEN 1 ELSE 0 END AS isPublic,
            ri.image_url AS imageUrl,
            COALESCE(AVG(rv.rating), 0) AS rating,
            COUNT(rv.review_id) AS reviewCount
        FROM bookmarks b
        JOIN restaurants r ON b.restaurant_id = r.restaurant_id
        LEFT JOIN restaurant_images ri
          ON ri.restaurant_id = r.restaurant_id
         AND ri.restaurant_image_id = (
            SELECT MIN(ri2.restaurant_image_id)
            FROM restaurant_images ri2
            WHERE ri2.restaurant_id = r.restaurant_id
          )
        LEFT JOIN reviews rv ON rv.restaurant_id = r.restaurant_id
        WHERE b.user_id = :userId
          AND b.bookmark_id = (
            SELECT MAX(b2.bookmark_id)
            FROM bookmarks b2
            WHERE b2.user_id = b.user_id AND b2.restaurant_id = b.restaurant_id
          )
        GROUP BY
          b.bookmark_id, r.restaurant_id, r.name, r.description, r.avg_main_price,
          r.reservation_limit, b.promotion_agree, b.is_public, ri.image_url
        ORDER BY b.bookmark_id DESC
        """, nativeQuery = true)
    List<BookmarkListRow> findBookmarksByUserId(Long userId);
}
