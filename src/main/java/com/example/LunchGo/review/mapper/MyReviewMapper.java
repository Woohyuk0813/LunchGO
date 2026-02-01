package com.example.LunchGo.review.mapper;

import com.example.LunchGo.review.mapper.row.MyReviewRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyReviewMapper {
    List<MyReviewRow> selectMyReviews(@Param("userId") Long userId);
}
