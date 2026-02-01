package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReviewIdAndRestaurantId(Long reviewId, Long restaurantId);
    Optional<Review> findTopByReservationIdAndUserIdOrderByCreatedAtDesc(Long reservationId, Long userId);
}
