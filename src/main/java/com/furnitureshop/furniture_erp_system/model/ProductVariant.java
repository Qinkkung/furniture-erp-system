package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <<< Import BigDecimal
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.core.type.TypeReference; // <<< เพิ่ม
import com.fasterxml.jackson.databind.ObjectMapper; // <<< เพิ่ม
import java.util.Collections; // <<< เพิ่ม
import java.util.Map; // <<< เพิ่ม
import jakarta.persistence.Transient;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
	
	@Transient // <<< บอก JPA ว่าไม่ต้องสร้างคอลัมน์นี้
	public Map<String, String> getAttributesMap() {
	    if (this.attributes == null || this.attributes.isEmpty()) {
	        return Collections.emptyMap(); // คืนค่า Map ว่าง ถ้าไม่มี attributes
	    }
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        // แปลง JSON string เป็น Map
	        return mapper.readValue(this.attributes, new TypeReference<Map<String, String>>() {});
	    } catch (Exception e) {
	        System.err.println("Error parsing attributes JSON for variant " + this.variantID + ": " + e.getMessage());
	        return Collections.emptyMap(); // คืนค่า Map ว่าง ถ้าแปลง Error
	    }
	}

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