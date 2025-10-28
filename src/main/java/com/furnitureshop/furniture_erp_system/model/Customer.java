package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * นี่คือคลาส Entity สำหรับตาราง 'Customers' (ข้อมูลลูกค้า)
 * มันตรงกับ Data Dictionary 3.9
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "customer_id")
    private String customerID; // ตรงกับ 3.9: Field 'CustomerID'

    public String getCustomerID() {
		return customerID;
	}


	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public DeliveryZone getDeliveryZone() {
		return deliveryZone;
	}


	public void setDeliveryZone(DeliveryZone deliveryZone) {
		this.deliveryZone = deliveryZone;
	}


	@Column(name = "name", nullable = false)
    private String name; // ตรงกับ 3.9: Field 'Name'

    @Column(name = "phone")
    private String phone; // ตรงกับ 3.9: Field 'Phone'

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // ตรงกับ 3.9: Field 'Address'

    
    // --- (นี่คือ Logic การเชื่อมโยงไปหา Zone) ---
    /**
     * ความสัมพันธ์ ManyToOne (ลูกค้าหลายคน อยู่ใน Zone เดียวกันได้)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    /**
     * @JoinColumn บอกว่าในตารางนี้ (customers)
     * จะมี Foreign Key ชื่อ "zone_id"
     * ที่อ้างอิงกลับไปหา Primary Key ของตาราง DeliveryZone
     */
    @JoinColumn(name = "zone_id") // ตรงกับ 3.9: Field 'ZoneID (FK)'
    private DeliveryZone deliveryZone; // ลูกค้า 1 คน สังกัด 1 Zone

    


}