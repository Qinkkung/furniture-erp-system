package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryID;

    public Integer getInventoryID() {
		return inventoryID;
	}

	public void setInventoryID(Integer inventoryID) {
		this.inventoryID = inventoryID;
	}

	public int getQuantityOnHand() {
		return quantityOnHand;
	}

	public void setQuantityOnHand(int quantityOnHand) {
		this.quantityOnHand = quantityOnHand;
	}

	public int getQuantityReserved() {
		return quantityReserved;
	}

	public void setQuantityReserved(int quantityReserved) {
		this.quantityReserved = quantityReserved;
	}

	public ProductVariant getProductVariant() {
		return productVariant;
	}

	public void setProductVariant(ProductVariant productVariant) {
		this.productVariant = productVariant;
	}

	@Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand = 0; // ควรมีค่าเริ่มต้น

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved = 0; // ควรมีค่าเริ่มต้น

    // --- Relationship ---
    @OneToOne(fetch = FetchType.LAZY, optional = false) // optional=false บังคับว่าต้องมี Variant
    @JoinColumn(name = "variant_id", nullable = false, unique = true)
    private ProductVariant productVariant;

    // --- Constructor ---
    public Inventory() {
    }

    // --- Logic คำนวณ ATS ---
    @Transient // บอก JPA ว่าไม่ต้องสร้างคอลัมน์นี้
    public int getAvailableToSell() {
        return this.quantityOnHand - this.quantityReserved;
    }

    
}