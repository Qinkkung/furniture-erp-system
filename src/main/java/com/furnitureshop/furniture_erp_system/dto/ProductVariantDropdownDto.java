package com.furnitureshop.furniture_erp_system.dto;

import java.math.BigDecimal; // Import เผื่อไว้

// คลาส DTO สำหรับแสดงข้อมูล Variant ใน Dropdown และตาราง Items
public class ProductVariantDropdownDto {
    private String variantID;
    private String skuCode;
    private String productName; // ชื่อ Product
    private String attributesJson; // Attributes (JSON String)
    private BigDecimal unitPrice; // ราคาขาย (เผื่อใช้)
    // Add costPerUnit if needed directly here from POItem DTO in future maybe?

    // Constructors (Optional but good practice)
    public ProductVariantDropdownDto() {}

    public ProductVariantDropdownDto(String variantID, String skuCode, String productName, String attributesJson, BigDecimal unitPrice) {
        this.variantID = variantID;
        this.skuCode = skuCode;
        this.productName = productName;
        this.attributesJson = attributesJson;
        this.unitPrice = unitPrice;
    }

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

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getAttributesJson() {
		return attributesJson;
	}

	public void setAttributesJson(String attributesJson) {
		this.attributesJson = attributesJson;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

    
}