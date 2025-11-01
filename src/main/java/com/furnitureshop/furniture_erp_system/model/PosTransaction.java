package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime; // (ใช้ LocalDateTime เพื่อเก็บเวลาที่ขาย)
import java.util.HashSet;
import java.util.Set;

/**
 * Entity สำหรับตาราง 'POS_Transactions' (หัวบิลขายหน้าร้าน)
 * ตรงกับ Data Dictionary 3.9 [cite: 382]
 */
@Entity
@Table(name = "pos_transactions")
public class PosTransaction {

    @Id
    @Column(name = "pos_trans_id")
    private String posTransID; // [cite: 383]

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate; // [cite: 383]

    @Column(name = "grand_total", precision = 12, scale = 2)
    private BigDecimal grandTotal; // [cite: 383]

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // [cite: 383]

    @OneToMany(
        mappedBy = "posTransaction",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<PosTransactionItem> posTransactionItems = new HashSet<>();
    
    // --- Getters and Setters ---
    public String getPosTransID() { return posTransID; }
    public void setPosTransID(String posTransID) { this.posTransID = posTransID; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Set<PosTransactionItem> getPosTransactionItems() { return posTransactionItems; }
    public void setPosTransactionItems(Set<PosTransactionItem> posTransactionItems) { this.posTransactionItems = posTransactionItems; }
}