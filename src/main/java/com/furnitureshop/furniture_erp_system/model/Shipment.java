package com.furnitureshop.furniture_erp_system.model;

import java.time.LocalDate; // ใช้ LocalDate สำหรับวันที่
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id; // ใช้ String ID สำหรับ ShipmentID
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne; // หนึ่ง SO มีได้หนึ่ง Shipment (โดยทั่วไป)
import jakarta.persistence.Table;

/**
 * Entity สำหรับตาราง 'Shipments' (ใบจัดส่ง).
 * เชื่อมโยงกับ SalesOrder และใช้ติดตามสถานะการจัดส่ง.
 * ตรงกับ Data Dictionary 3.9.
 */
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @Column(name = "shipment_id")
    private String shipmentID; // ตรงกับ 3.9: Field 'ShipmentID'

    public String getShipmentID() {
		return shipmentID;
	}


	public void setShipmentID(String shipmentID) {
		this.shipmentID = shipmentID;
	}


	public LocalDate getShipDate() {
		return shipDate;
	}


	public void setShipDate(LocalDate shipDate) {
		this.shipDate = shipDate;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public SalesOrder getSalesOrder() {
		return salesOrder;
	}


	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}


	@Column(name = "ship_date")
    private LocalDate shipDate; // ตรงกับ 3.9: Field 'ShipDate' (วันที่กำหนดส่ง)

    @Column(name = "status", nullable = false)
    private String status; // ตรงกับ 3.9: Field 'Status' (Pending, Shipped, Delivered)


    // --- ความสัมพันธ์กับ SalesOrder ---
    /**
     * OneToOne: แต่ละ Shipment จะถูกสร้างมาจาก SalesOrder เพียงใบเดียว.
     * FetchType.LAZY: ไม่ต้องโหลด SO มา ถ้ายังไม่เรียกใช้.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true) // FK อ้างอิง SalesOrder, ต้อง unique
    private SalesOrder salesOrder;


    // --- Getters and Setters ---
    // (สร้างขั้นตอนต่อไป)

}