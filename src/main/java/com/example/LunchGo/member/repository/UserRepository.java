package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 이미 존재하는 이메일인지 확인
     */
    boolean existsByEmail(String email);

    /**
     * name과 phone으로 사용자 email 찾기
     * */
    Optional<User> findByNameAndPhone(String name, String phone);

    /**
     * email, name, phone으로 사용자 존재 확인(비밀번호 찾기에서 사용)
     * */
    boolean existsByEmailAndNameAndPhone(String email, String name, String phone);

    /**
     * 인증된 email로 비밀번호 변경
     * */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    int updatePassword(@Param("email") String email, @Param("password") String password);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.status = 'DORMANT' WHERE u.lastLoginAt < :current AND u.status = 'ACTIVE'")
    int updateDormantUsers(@Param("current") LocalDateTime current);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.userId = :userId")
    int updateLastLoginAt(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM User u WHERE u.status='WITHDRAWAL' and u.withdrawalAt <= :current")
    void deleteUserComplete(@Param("current") LocalDateTime current);

    /**
     * email로 사용자 정보 뽑아내기 (임직원 등록 시 사용)
     * */
    Optional<User> findByEmail(String email);

    List<User> findTop10ByEmailContainingIgnoreCaseOrderByEmailAsc(String email);

    /**
     * 사업자에게 예약 완료 문자 알림시 사용자 정보 필요
     * */
    Optional<User> findByUserId(Long userId);

    /**
     * 이메일 인증 후 바로 DB에 email_authentication 반영
     * */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.emailAuthentication = true WHERE u.userId = :userId")
    int updateEmailAuthentication(@Param("userId") Long userId);
}
