package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByRestaurantIdAndOwnerId(Long restaurantId, Long ownerId);

    @Query("SELECT r.restaurantId FROM Restaurant r WHERE r.ownerId = :ownerId")
    Optional<Long> findRestaurantIdByOwnerId(@Param("ownerId") Long ownerId);

    // 식당이 설정한 예약석 수의 최대 한도를 반환
    @Query("SELECT r.reservationLimit FROM Restaurant r WHERE r.restaurantId = :restaurantId")
    Optional<Integer> findReservationLimitByRestaurantId(Long restaurantId);

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.restaurantImages WHERE r.restaurantId = :restaurantId")
    Optional<Restaurant> findByIdWithImages(@Param("restaurantId") Long restaurantId);

    @Query(value = "SELECT tag_id FROM restaurant_tag_maps WHERE restaurant_id = :restaurantId", nativeQuery = true)
    List<Long> findTagIdsByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query(
            value = "SELECT COUNT(DISTINCT tm.tag_id) " +
                    "FROM speciality_mappings sm " +
                    "JOIN tag_maps tm ON tm.specialty_id = sm.speciality_id " +
                    "WHERE sm.user_id = :userId AND tm.weight = 1",
            nativeQuery = true
    )
    int countLikedTagsByUserId(@Param("userId") Long userId);

    @Query(
            value = "SELECT COUNT(DISTINCT tm.tag_id) " +
                    "FROM speciality_mappings sm " +
                    "JOIN tag_maps tm ON tm.specialty_id = sm.speciality_id " +
                    "WHERE sm.user_id = :userId AND tm.weight = 0",
            nativeQuery = true
    )
    int countDislikedTagsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO restaurant_tag_maps (restaurant_id, tag_id) VALUES (:restaurantId, :tagId)", nativeQuery = true)
    void saveRestaurantTagMapping(@Param("restaurantId") Long restaurantId, @Param("tagId") Long tagId);

    @Query(
            value = "WITH user_specialities AS (" +
                    "SELECT sm.speciality_id " +
                    "FROM speciality_mappings sm " +
                    "WHERE sm.user_id = :userId" +
                    ") , liked_tags AS (" +
                    "SELECT DISTINCT tm.tag_id " +
                    "FROM user_specialities us " +
                    "JOIN tag_maps tm ON tm.specialty_id = us.speciality_id " +
                    "WHERE tm.weight = 1" +
                    ") , disliked_tags AS (" +
                    "SELECT DISTINCT tm.tag_id " +
                    "FROM user_specialities us " +
                    "JOIN tag_maps tm ON tm.specialty_id = us.speciality_id " +
                    "WHERE tm.weight = 0" +
                    ") , restaurant_tag_agg AS (" +
                    "SELECT " +
                    "rtm.restaurant_id, " +
                    "COUNT(*) AS restaurant_tag_count, " +
                    "SUM(CASE WHEN lt.tag_id IS NOT NULL THEN 1 ELSE 0 END) AS liked_match_count, " +
                    "SUM(CASE WHEN dt.tag_id IS NOT NULL THEN 1 ELSE 0 END) AS disliked_match_count " +
                    "FROM restaurant_tag_maps rtm " +
                    "JOIN restaurants r ON r.restaurant_id = rtm.restaurant_id " +
                    "LEFT JOIN liked_tags lt ON lt.tag_id = rtm.tag_id " +
                    "LEFT JOIN disliked_tags dt ON dt.tag_id = rtm.tag_id " +
                    "WHERE r.status = 'OPEN' " +
                    "GROUP BY rtm.restaurant_id" +
                    ") " +
                    "SELECT r.restaurant_id AS restaurantId, " +
                    "COALESCE(1.0 * rta.liked_match_count / " +
                    "NULLIF(:likedCount + rta.restaurant_tag_count - rta.liked_match_count, 0), 0) AS likeScore, " +
                    "COALESCE(1.0 * rta.disliked_match_count / NULLIF(:dislikedCount, 0), 0) AS dislikePenalty, " +
                    "100 * POW(COALESCE(1.0 * rta.liked_match_count / " +
                    "NULLIF(:likedCount + rta.restaurant_tag_count - rta.liked_match_count, 0), 0), 2) " +
                    "- CASE WHEN rta.disliked_match_count > 0 THEN 30 ELSE 0 END " +
                    "- 80 * COALESCE(1.0 * rta.disliked_match_count / NULLIF(:dislikedCount, 0), 0) AS finalScore " +
                    "FROM restaurants r " +
                    "JOIN restaurant_tag_agg rta ON rta.restaurant_id = r.restaurant_id " +
                    "WHERE (:likedCount + :dislikedCount) > 0 " +
                    "ORDER BY finalScore DESC " +
                    "LIMIT 50",
            nativeQuery = true
    )
    List<RestaurantSimilarityProjection> findRestaurantsByUserTagSimilarity(
            @Param("userId") Long userId,
            @Param("likedCount") int likedCount,
            @Param("dislikedCount") int dislikedCount
    );


    @Query(
            value = "SELECT " +
                    "r.restaurant_id AS restaurantId, " +
                    "r.name AS name, " +
                    "r.road_address AS roadAddress, " +
                    "r.detail_address AS detailAddress, " +
                    "COALESCE(s.view_recent, 0) AS viewCount, " +
                    "COALESCE(s.confirm_recent, 0) AS confirmCount, " +
                    "COALESCE(rv.reviewCount, 0) AS reviewCount, " +
                    "COALESCE(rv.rating, 0) AS rating, " +
                    "(" +
                    "(COALESCE(s.confirm_recent, 0) * :confirmWeight) + " +
                    "(COALESCE(s.view_recent, 0) * :viewWeight) + " +
                    "CASE " +
                    "WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                    "THEN :newbieWeight " +
                    "WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 60 DAY) " +
                    "THEN :newbieWeight * (60 - DATEDIFF(CURDATE(), r.created_at)) / 30 " +
                    "ELSE 0 " +
                    "END" +
                    ") AS score, " +
                    "img.imageUrl AS imageUrl, " +
                    "rt.tagIds AS tagIds, " +
                    "rt.tagContents AS tagContents, " +
                    "rvt.reviewTagIds AS reviewTagIds, " +
                    "rvt.reviewTagContents AS reviewTagContents, " +
                    "rvt.reviewTagCounts AS reviewTagCounts " +
                    "FROM restaurants r " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "CAST(SUM(IFNULL(view_count, 0)) AS SIGNED) AS view_recent, " +
                    "CAST(SUM(IFNULL(confirm_count, 0)) AS SIGNED) AS confirm_recent " +
                    "FROM daily_restaurant_stats " +
                    "WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
                    "GROUP BY restaurant_id" +
                    ") s ON r.restaurant_id = s.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "COUNT(*) AS reviewCount, " +
                    "ROUND(AVG(rating), 1) AS rating " +
                    "FROM reviews " +
                    "WHERE status = 'PUBLIC' " +
                    "GROUP BY restaurant_id" +
                    ") rv ON r.restaurant_id = rv.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(image_url ORDER BY restaurant_image_id), ',', 1) AS imageUrl " +
                    "FROM restaurant_images " +
                    "GROUP BY restaurant_id" +
                    ") img ON r.restaurant_id = img.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT rtm.restaurant_id, " +
                    "GROUP_CONCAT(st.tag_id ORDER BY st.tag_id SEPARATOR ',') AS tagIds, " +
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
                    "ORDER BY score DESC " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<TrendingRestaurantProjection> findTrendingRestaurants(
            @Param("days") int days,
            @Param("limit") int limit,
            @Param("newbieWeight") double newbieWeight,
            @Param("confirmWeight") double confirmWeight,
            @Param("viewWeight") double viewWeight
    );
    @Modifying
    @Query(value = "DELETE FROM restaurant_tag_maps WHERE restaurant_id = :restaurantId", nativeQuery = true)
    void deleteRestaurantTagMappingsByRestaurantId(@Param("restaurantId") Long restaurantId);
}
