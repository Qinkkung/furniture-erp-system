package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <<< Import BigDecimal
import java.util.Objects; // <<< Import Objects for null check

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemID;

    public Integer getOrderItemID() {
		return orderItemID;
	}

	public void setOrderItemID(Integer orderItemID) {
		this.orderItemID = orderItemID;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public SalesOrder getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}

	public ProductVariant getProductVariant() {
		return productVariant;
	}

	public void setProductVariant(ProductVariant productVariant) {
		this.productVariant = productVariant;
	}

	@Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // <<< ใช้ BigDecimal

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;

    // --- Constructor ---
    public SalesOrderItem() {
    }

    // --- (Method ใหม่ สำหรับคำนวณราคารวม) ---
    /**
     * คำนวณราคารวมสำหรับรายการสินค้านี้ (ราคาต่อหน่วย * จำนวน)
     * @return BigDecimal ของราคารวม หรือ BigDecimal.ZERO ถ้า unitPrice เป็น null
     */
    @Transient // <<< บอก JPA ว่าไม่ต้องสร้างคอลัมน์นี้
    public BigDecimal getItemTotalPrice() {
        // เพิ่มการตรวจสอบ null ให้ปลอดภัยยิ่งขึ้น
        if (this.unitPrice == null || this.quantity <= 0) {
            return BigDecimal.ZERO;
        }
        // ใช้ BigDecimal.valueOf(long) เพื่อความชัดเจน
        return this.unitPrice.multiply(BigDecimal.valueOf((long)this.quantity));
    }


}