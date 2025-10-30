package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne; // <<< เปลี่ยนจาก OneToOne
import jakarta.persistence.Table;

/**
 * Entity for the 'Quality_Checks' table.
 * Records the QC result for a specific PurchaseOrderItem received.
 * Matches Data Dictionary 3.9.
 */
@Entity
@Table(name = "quality_checks")
public class QualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qc_id")
    private Integer qcID; // Matches 3.9: Field 'QC_ID' (PK)

    @Column(name = "status", nullable = false)
    private String status; // Matches 3.9: Field 'Status' (Pending, Pass, Fail, B-Grade)

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Matches 3.9: Field 'Notes'

    // --- Relationship to PurchaseOrderItem ---
    /**
     * *** แก้ไข: เปลี่ยนเป็น ManyToOne ***
     * QC "หลาย" ครั้ง สามารถเชื่อมโยงไปยัง PO Item "หนึ่ง" รายการได้
     * (เช่น รับของล็อตแรก 5 ชิ้น QC Pass, ล็อตสอง 5 ชิ้น QC Fail)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // <<< แก้เป็น @ManyToOne
    @JoinColumn(name = "po_item_id", nullable = false) // <<< ลบ unique = true ออก
    private PurchaseOrderItem purchaseOrderItem;

    // --- Constructor ---
    public QualityCheck() {
    }

    // --- Getters and Setters ---
    // (*** อย่าลืม! ลบของเก่าทิ้ง แล้ว Generate Getters/Setters ใหม่ทั้งหมด ***)
    public Integer getQcID() {
        return qcID;
    }

    public void setQcID(Integer qcID) {
        this.qcID = qcID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PurchaseOrderItem getPurchaseOrderItem() {
        return purchaseOrderItem;
    }

    public void setPurchaseOrderItem(PurchaseOrderItem purchaseOrderItem) {
        this.purchaseOrderItem = purchaseOrderItem;
    }
}