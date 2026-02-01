package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {
    void deleteByReceiptId(Long receiptId);
}
