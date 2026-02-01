package com.example.LunchGo.review.mapper;

import com.example.LunchGo.review.dto.CommentResponse;
import com.example.LunchGo.review.dto.ReceiptItemResponse;
import com.example.LunchGo.review.dto.ReviewAdminItemResponse;
import com.example.LunchGo.review.dto.ReviewDetailResponse;
import com.example.LunchGo.review.dto.ReviewImageRow;
import com.example.LunchGo.review.dto.ReviewItemResponse;
import com.example.LunchGo.review.dto.ReviewListQuery;
import com.example.LunchGo.review.dto.ReviewSummary;
import com.example.LunchGo.review.dto.ReviewTagRow;
import com.example.LunchGo.review.dto.TagCount;
import com.example.LunchGo.review.dto.TagResponse;
import com.example.LunchGo.review.dto.VisitInfo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewReadMapper {
    ReviewSummary selectReviewSummary(@Param("restaurantId") Long restaurantId, @Param("includeBlinded") boolean includeBlinded);

    List<TagCount> selectTopTags(@Param("restaurantId") Long restaurantId, @Param("includeBlinded") boolean includeBlinded);

    long selectReviewCount(ReviewListQuery query);

    List<ReviewItemResponse> selectReviewItems(ReviewListQuery query);

    List<ReviewItemResponse> selectReviewItemsPage(ReviewListQuery query);

    List<Long> selectReviewPageIds(ReviewListQuery query);

    List<ReviewItemResponse> selectReviewItemsByIds(@Param("reviewIds") List<Long> reviewIds);

    ReviewDetailResponse selectReviewDetail(@Param("reviewId") Long reviewId, @Param("includeBlinded") boolean includeBlinded);

    VisitInfo selectVisitInfo(@Param("reviewId") Long reviewId);

    List<ReceiptItemResponse> selectReceiptItems(@Param("receiptId") Long receiptId);

    List<ReceiptItemResponse> selectReceiptItemsByReviewId(@Param("reviewId") Long reviewId);

    List<TagResponse> selectReviewTags(@Param("reviewId") Long reviewId);

    List<String> selectReviewImages(@Param("reviewId") Long reviewId);

    List<CommentResponse> selectReviewComments(@Param("reviewId") Long reviewId);

    CommentResponse selectReviewCommentById(@Param("commentId") Long commentId);

    List<ReviewTagRow> selectReviewTagsByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    List<ReviewImageRow> selectReviewImagesByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    List<ReviewAdminItemResponse> selectAdminReviews();
}
