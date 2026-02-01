package com.example.LunchGo.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "receipt_items")
public class ReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_item_id")
    private Long receiptItemId;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(name = "menu_name", nullable = false)
    private String menuName;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    public ReceiptItem(Long receiptId, String menuName, Integer qty, Integer unitPrice, Integer totalPrice) {
        this.receiptId = receiptId;
        this.menuName = menuName;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }
}
