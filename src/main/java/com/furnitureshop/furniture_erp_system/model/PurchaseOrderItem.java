package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set; // <<< Import Set
import java.util.HashSet; // <<< Import HashSet

@Entity
@Table(name = "po_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "po_item_id")
    private Integer poItemID;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "cost_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;
    
    // --- *** เพิ่มความสัมพันธ์ฝั่งตรงข้าม *** ---
    /**
     * OneToMany (PO Item 1 อัน มี QC record ได้หลายอัน)
     */
    @OneToMany(
        mappedBy = "purchaseOrderItem",
        cascade = CascadeType.ALL, // ถ้าลบ PO Item ให้ลบ QC Record ด้วย
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<QualityCheck> qualityChecks = new HashSet<>(); // <<< Initialize Set

    // --- Constructor ---
    public PurchaseOrderItem() {
    }

    // --- Getters and Setters ---
    // (*** อย่าลืม! ลบของเก่าทิ้ง แล้ว Generate Getters/Setters ใหม่ทั้งหมด ***)
    public Integer getPoItemID() {
        return poItemID;
    }

    public void setPoItemID(Integer poItemID) {
        this.poItemID = poItemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCostPerUnit() {
        return costPerUnit;
    }

    public void setCostPerUnit(BigDecimal costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

    public void setProductVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    public Set<QualityCheck> getQualityChecks() { // <<< เพิ่ม
        return qualityChecks;
    }

    public void setQualityChecks(Set<QualityCheck> qualityChecks) { // <<< เพิ่ม
        this.qualityChecks = qualityChecks;
    }
}