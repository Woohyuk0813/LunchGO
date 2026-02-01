package com.example.LunchGo.review.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitInfo {
    private LocalDate date;
    private Integer partySize;
    private Integer totalAmount;
    private String receiptImageUrl;
    private List<ReceiptItemResponse> menuItems;
}
