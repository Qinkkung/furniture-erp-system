package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity สำหรับตาราง 'POS_Transaction_Items' (รายการในบิล POS)
 * ตรงกับ Data Dictionary 3.9 [cite: 384]
 */
@Entity
@Table(name = "pos_transaction_items")
public class PosTransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pos_item_id")
    private Integer posItemID; // [cite: 385]

    @Column(name = "quantity", nullable = false)
    private int quantity; // [cite: 385]

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // [cite: 385] (ราคาที่ขาย ณ ตอนนั้น)

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pos_trans_id", nullable = false)
    private PosTransaction posTransaction; // [cite: 385]

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant; // [cite: 385]

    // --- Getters and Setters ---
    public Integer getPosItemID() { return posItemID; }
    public void setPosItemID(Integer posItemID) { this.posItemID = posItemID; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public PosTransaction getPosTransaction() { return posTransaction; }
    public void setPosTransaction(PosTransaction posTransaction) { this.posTransaction = posTransaction; }
    public ProductVariant getProductVariant() { return productVariant; }
    public void setProductVariant(ProductVariant productVariant) { this.productVariant = productVariant; }
}