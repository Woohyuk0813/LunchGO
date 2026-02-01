package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    /**
     * Spring Security
     * */
    boolean existsByLoginId(String loginId);

    Optional<Manager> findByLoginId(String loginId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Manager m SET m.lastLoginAt = CURRENT_TIMESTAMP WHERE m.managerId = :managerId")
    int updateLastLoginAt(@Param("managerId") Long managerId);
}
