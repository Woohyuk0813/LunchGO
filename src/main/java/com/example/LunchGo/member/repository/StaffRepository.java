package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    @Modifying
    @Query("DELETE FROM Staff s WHERE s.staffId = :staffId AND s.ownerId = :ownerId")
    int deleteByStaffId(Long staffId, Long ownerId);

    boolean existsByEmail(String email);

    List<Staff> searchByOwnerId(Long ownerId);

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByStaffId(Long staffId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Staff s SET s.lastLoginAt = CURRENT_TIMESTAMP WHERE s.staffId = :staffId")
    int updateLastLoginAt(@Param("staffId") Long staffId);
}
