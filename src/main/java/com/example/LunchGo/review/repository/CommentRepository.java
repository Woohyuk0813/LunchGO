package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.Comment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCommentIdAndReviewId(Long commentId, Long reviewId);
}
