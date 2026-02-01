package com.example.LunchGo.member.mapper;

import com.example.LunchGo.member.dto.MemberInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MemberMapper {
    /**
     * 마이페이지에서 유저 정보 가져오기
     * */
    MemberInfo selectUser(@Param("userId") Long userId);


    /**
     * 마이페이지에서 유저 정보 업데이트(특이사항 제외)
     * */
    Integer updateUser(@Param("userId") Long userId,
                       @Param("nickname") String nickname, @Param("birth") LocalDate birth,
                       @Param("gender") String gender, @Param("phone") String phone,
                       @Param("companyName") String companyName, @Param("companyAddress") String companyAddress,
                       @Param("emailAuthentication") Boolean emailAuthentication, @Param("image") String image);

    /**
     * 마이페이지에서 유저 정보 업데이트(특이사항 업데이트)
     * */
    void deleteUserSpecialities(@Param("userId") Long userId);

    void insertUserSpecialities(@Param("userId") Long userId, @Param("specialityIds") List<Long> specialityIds);

    /**
     * 사업자가 프로모션 보낼 시, 즐겨찾기 등록한 사용자 email 알아야함
     * */
    List<String> getPromotionEmails(@Param("ownerId") Long ownerId);
}
