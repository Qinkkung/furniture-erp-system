package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * นี่คือคลาส Entity สำหรับตาราง 'Suppliers' (ข้อมูลซัพพลายเออร์).
 * เป็นตารางหลัก (Master Table) ใช้อ้างอิงใน PO.
 * ตรงกับ Data Dictionary 3.9.
 * @Entity บอก Spring Boot ว่าคลาสนี้คือ "ตาราง" ในฐานข้อมูล.
 */
@Entity
@Table(name = "suppliers")
public class Supplier {

    /**
     * @Id บอกว่าฟิลด์นี้คือ Primary Key (PK).
     */
    @Id
    @Column(name = "supplier_id")
    private String supplierID; // ตรงกับ 3.9: Field 'SupplierID', Type 'VARCHAR(10)'

    public String getSupplierID() {
		return supplierID;
	}

	public void setSupplierID(String supplierID) {
		this.supplierID = supplierID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@Column(name = "name", nullable = false)
    private String name; // ตรงกับ 3.9: Field 'Name', Type 'VARCHAR(255)'

    @Column(name = "contact")
    private String contact; // ตรงกับ 3.9: Field 'Contact', Type 'VARCHAR(100)'

 

}