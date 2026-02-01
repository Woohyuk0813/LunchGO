package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.ReviewTagMap;
import com.example.LunchGo.review.entity.ReviewTagMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTagMapRepository extends JpaRepository<ReviewTagMap, ReviewTagMapId> {
    void deleteByIdReviewId(Long reviewId);
}
