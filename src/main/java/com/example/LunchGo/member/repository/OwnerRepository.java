package com.example.LunchGo.member.repository;

import com.example.LunchGo.member.entity.Owner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    /**
     * 이미 존재하는 아이디인지 확인
     * */
    boolean existsByLoginId(String loginId);

    /**
     * name, businessNum, phone으로 아이디 찾기
     * */
    Optional<Owner> findByNameAndBusinessNumAndPhone(String name, String businessNum, String phone);

    /**
     * loginId, name, phone으로 사업자 존재 확인
     * */
    boolean existsByLoginIdAndNameAndPhone(String loginId, String name, String phone);

    /**
     * 인증된 loginId로 비밀번호 변경
     * */
    @Modifying(clearAutomatically = true) //영속성 컨텍스트 초기화
    @Query("UPDATE Owner o SET o.password = :password WHERE o.loginId = :loginId")
    int updatePassword(@Param("loginId") String loginId, @Param("password") String password);

    /**
     * 마이페이지에서 사업자 정보 가져오기
     * */
    Optional<Owner> findByOwnerId(Long ownerId);

    /**
     * 마이페이지에서 사업자 정보 변경
     * */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Owner o SET o.phone = :phone, o.image = :image WHERE o.ownerId = :ownerId")
    int updateOwner(@Param("ownerId") Long ownerId, @Param("phone") String phone, @Param("image") String image);

    /**
     * Spring Security
     * */
    Optional<Owner> findByLoginId(String loginId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Owner o SET o.lastLoginAt = CURRENT_TIMESTAMP WHERE o.ownerId = :ownerId")
    int updateLastLoginAt(@Param("ownerId") Long ownerId);

    /**
     * Scheduler
     * */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Owner o WHERE o.status='WITHDRAWAL' AND o.withdrawalAt <=:current")
    void deleteOwnerComplete(@Param("current") LocalDateTime current);

    /**
     * 식당을 등록하지 않은 활성 상태의 사업자 중 특정 기간 내에 가입한 전화번호 목록 조회
     * */
    @Query("SELECT o.phone FROM Owner o LEFT JOIN Restaurant r ON o.ownerId = r.ownerId " +
            "WHERE o.status = 'ACTIVE' AND r.restaurantId IS NULL AND o.createdAt BETWEEN :startDateTime AND :endDateTime")
    List<String> findPhonesByActiveAndNoRestaurant(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 식당을 등록하지 않은 활성 상태의 사업자 중 가입한 지 특정 시간이 지난 사업자 ID 목록 조회
     * */
    @Query("SELECT o.ownerId FROM Owner o LEFT JOIN Restaurant r ON o.ownerId = r.ownerId " +
            "WHERE o.status = 'ACTIVE' AND r.restaurantId IS NULL AND o.createdAt <= :targetTime")
    List<Long> findOwnerIdsByActiveAndNoRestaurantAndExpired(@Param("targetTime") LocalDateTime targetTime);

    /**
     * 특정 사업자 회원 탈퇴 처리
     * */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Owner o SET o.status = 'WITHDRAWAL', o.withdrawalAt = CURRENT_TIMESTAMP WHERE o.ownerId = :ownerId")
    int withdrawOwner(@Param("ownerId") Long ownerId);
}
