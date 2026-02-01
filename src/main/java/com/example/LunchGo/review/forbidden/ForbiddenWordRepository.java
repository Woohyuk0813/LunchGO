package com.example.LunchGo.review.forbidden;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWord, Long> {
    List<ForbiddenWord> findByEnabledTrue();
}
