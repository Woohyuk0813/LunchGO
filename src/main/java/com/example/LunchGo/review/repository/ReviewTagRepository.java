package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {
}
