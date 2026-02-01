package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.ReceiptDTO;
import com.example.LunchGo.review.entity.Receipt;
import com.example.LunchGo.review.entity.ReceiptItem;
import com.example.LunchGo.review.repository.ReceiptItemRepository;
import com.example.LunchGo.review.repository.ReceiptRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptItemRepository receiptItemRepository;

    @Transactional
    public Receipt createReceipt(Long reservationId, String receiptImageKey, ReceiptDTO receiptDTO) {
        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is required");
        }
        if (receiptDTO == null) {
            throw new IllegalArgumentException("receipt data is required");
        }

        Receipt saved = receiptRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
            .map(existing -> {
                existing.updateConfirmedAmount(receiptDTO.getTotalAmount());
                existing.updateImageUrl(receiptImageKey);
                receiptItemRepository.deleteByReceiptId(existing.getReceiptId());
                return existing;
            })
            .orElseGet(() -> receiptRepository.save(new Receipt(reservationId, receiptDTO.getTotalAmount(), receiptImageKey)));

        List<ReceiptItem> items = new ArrayList<>();
        if (receiptDTO.getItems() != null) {
            for (ReceiptDTO.Item item : receiptDTO.getItems()) {
                int qty = Math.max(item.getQuantity(), 1);
                int unitPrice = Math.max(item.getPrice(), 0);
                int totalPrice = unitPrice * qty;
                items.add(new ReceiptItem(saved.getReceiptId(), item.getName(), qty, unitPrice, totalPrice));
            }
        }

        if (!items.isEmpty()) {
            receiptItemRepository.saveAll(items);
        }

        return saved;
    }
}
