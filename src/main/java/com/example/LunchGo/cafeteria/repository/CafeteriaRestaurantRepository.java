package com.example.LunchGo.cafeteria.repository;

import com.example.LunchGo.restaurant.entity.Restaurant;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CafeteriaRestaurantRepository extends Repository<Restaurant, Long> {

    @Query(
        value = "SELECT " +
            "r.restaurant_id AS restaurantId, " +
            "r.name AS name, " +
            "r.road_address AS roadAddress, " +
            "r.detail_address AS detailAddress, " +
            "r.avg_main_price AS avgMainPrice, " +
            "COALESCE(rv.reviewCount, 0) AS reviewCount, " +
            "COALESCE(rv.rating, 0) AS rating, " +
            "img.imageUrl AS imageUrl, " +
            "rt.tagContents AS tagContents, " +
            "rvt.reviewTagContents AS reviewTagContents, " +
            "rvt.reviewTagCounts AS reviewTagCounts " +
            "FROM restaurants r " +
            "LEFT JOIN (" +
            "SELECT restaurant_id, COUNT(*) AS reviewCount, ROUND(AVG(rating), 1) AS rating " +
            "FROM reviews WHERE status = 'PUBLIC' GROUP BY restaurant_id" +
            ") rv ON r.restaurant_id = rv.restaurant_id " +
            "LEFT JOIN (" +
            "SELECT restaurant_id, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(image_url ORDER BY restaurant_image_id), ',', 1) AS imageUrl " +
            "FROM restaurant_images GROUP BY restaurant_id" +
            ") img ON r.restaurant_id = img.restaurant_id " +
            "LEFT JOIN (" +
            "SELECT rtm.restaurant_id, " +
            "GROUP_CONCAT(st.content ORDER BY st.tag_id SEPARATOR ',') AS tagContents " +
            "FROM restaurant_tag_maps rtm " +
            "JOIN search_tags st ON st.tag_id = rtm.tag_id " +
            "GROUP BY rtm.restaurant_id" +
            ") rt ON r.restaurant_id = rt.restaurant_id " +
            "LEFT JOIN (" +
            "SELECT restaurant_id, " +
            "GROUP_CONCAT(tag_id ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagIds, " +
            "GROUP_CONCAT(name ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagContents, " +
            "GROUP_CONCAT(tag_count ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagCounts " +
            "FROM (" +
            "SELECT " +
            "rv.restaurant_id, " +
            "rt.tag_id, " +
            "rt.name, " +
            "COUNT(*) AS tag_count, " +
            "ROW_NUMBER() OVER ( " +
            "PARTITION BY rv.restaurant_id " +
            "ORDER BY COUNT(*) DESC, rt.tag_id " +
            ") AS rn " +
            "FROM reviews rv " +
            "JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id " +
            "JOIN review_tags rt ON rt.tag_id = rtm.tag_id " +
            "WHERE rv.status = 'PUBLIC' AND rt.tag_type = 'USER' " +
            "GROUP BY rv.restaurant_id, rt.tag_id, rt.name" +
            ") ranked " +
            "WHERE rn <= 3 " +
            "GROUP BY restaurant_id" +
            ") rvt ON r.restaurant_id = rvt.restaurant_id " +
            "WHERE r.status = 'OPEN' " +
            "ORDER BY COALESCE(rv.rating, 0) DESC, COALESCE(rv.reviewCount, 0) DESC, r.avg_main_price ASC " +
            "LIMIT :limit",
        nativeQuery = true
    )
    List<CafeteriaRestaurantProjection> findCandidateRestaurants(@Param("limit") int limit);

}
