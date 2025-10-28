package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <<< Import BigDecimal
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "payment_id", length = 20) // อาจจะต้องยาวขึ้น
    private String paymentID;

    public String getPaymentID() {
		return paymentID;
	}

	public void setPaymentID(String paymentID) {
		this.paymentID = paymentID;
	}

	public LocalDate getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public SalesOrder getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}

	@Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount; // <<< แก้เป็น BigDecimal

    @Column(name = "payment_type", nullable = false, length = 20)
    private String paymentType; // (Deposit, Full)

    // --- Relationship ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder salesOrder; // <<< แก้ Type และชื่อตัวแปร

    // --- Constructor ---
    public Payment() {
    }

   
}