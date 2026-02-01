package com.example.LunchGo.cafeteria.repository;

import com.example.LunchGo.cafeteria.entity.CafeteriaMenu;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CafeteriaMenuRepository extends JpaRepository<CafeteriaMenu, Long> {
    Optional<CafeteriaMenu> findByUserIdAndServedDate(Long userId, LocalDate servedDate);

    List<CafeteriaMenu> findByUserIdAndServedDateBetween(Long userId, LocalDate start, LocalDate end);
}
