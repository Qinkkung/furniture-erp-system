package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne; // Use OneToOne as one PO Item gets one QC check
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

	@Column(name = "status", nullable = false)
    private String status; // Matches 3.9: Field 'Status' (Pending, Pass, Fail, B-Grade)

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Matches 3.9: Field 'Notes'

    // --- Relationship to PurchaseOrderItem ---
    /**
     * OneToOne: Each QC record corresponds to exactly one PurchaseOrderItem received.
     * FetchType.LAZY: Don't load the PO Item unless needed.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_item_id", nullable = false, unique = true) // FK referencing PO_Item, must be unique
    private PurchaseOrderItem purchaseOrderItem;


}