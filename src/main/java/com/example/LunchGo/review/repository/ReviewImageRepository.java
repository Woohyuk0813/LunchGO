package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    void deleteByReviewId(Long reviewId);
}
