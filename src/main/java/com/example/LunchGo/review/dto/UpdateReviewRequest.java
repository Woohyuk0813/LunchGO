package com.example.LunchGo.review.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import com.example.LunchGo.review.validation.ForbiddenWordCheck;

@Getter
@Setter
public class UpdateReviewRequest {
    private Long receiptId;
    private Integer rating;
    @ForbiddenWordCheck
    private String content;
    private List<Long> tagIds;
    private List<String> imageUrls;
    private List<ReceiptItemRequest> receiptItems;

    @Getter
    @Setter
    public static class ReceiptItemRequest {
        private String name;
        private Integer quantity;
        private Integer price;
    }
}
