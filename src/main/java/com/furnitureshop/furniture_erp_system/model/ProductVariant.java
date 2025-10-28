package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <<< Import BigDecimal
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @Column(name = "variant_id", length = 10)
    private String variantID;

    public String getVariantID() {
		return variantID;
	}

	public void setVariantID(String variantID) {
		this.variantID = variantID;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	@Column(name = "sku_code", unique = true, nullable = false, length = 50)
    private String skuCode;

    @Column(name = "attributes", columnDefinition = "json") // สำหรับ PostgreSQL
    // @Column(name = "attributes", columnDefinition = "TEXT") // หรือใช้ TEXT ถ้าเป็น MySQL
    private String attributes;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2) // กำหนด precision/scale
    private BigDecimal unitPrice; // <<< แก้เป็น BigDecimal

    // --- Relationship ---
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional=false บังคับว่าต้องมี Product
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // --- Constructor ---
    public ProductVariant() {
    }

    
}