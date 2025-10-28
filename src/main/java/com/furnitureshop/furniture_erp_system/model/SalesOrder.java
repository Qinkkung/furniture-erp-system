package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <<< Import BigDecimal
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @Column(name = "order_id", length = 20) // อาจจะต้องยาวขึ้น
    private String orderID;

    public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	public BigDecimal getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(BigDecimal grandTotal) {
		this.grandTotal = grandTotal;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<SalesOrderItem> getSalesOrderItems() {
		return salesOrderItems;
	}

	public void setSalesOrderItems(Set<SalesOrderItem> salesOrderItems) {
		this.salesOrderItems = salesOrderItems;
	}

	public Set<Payment> getPayments() {
		return payments;
	}

	public void setPayments(Set<Payment> payments) {
		this.payments = payments;
	}

	@Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "grand_total", precision = 12, scale = 2) // กำหนด precision/scale
    private BigDecimal grandTotal; // <<< แก้เป็น BigDecimal

    @Column(name = "status", nullable = false, length = 30)
    private String status; // (Pending Deposit, Awaiting Shipment, ...)

    // --- Relationships ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // พนักงานขาย

    @OneToMany(
        mappedBy = "salesOrder",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<SalesOrderItem> salesOrderItems = new HashSet<>();

    @OneToMany( // SO หนึ่งใบอาจมี Payment หลายครั้ง (มัดจำ+ที่เหลือ)
        mappedBy = "salesOrder",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<Payment> payments = new HashSet<>(); // <<< เพิ่มความสัมพันธ์ Payment

    // --- Constructor ---
    public SalesOrder() {
    }

  
}