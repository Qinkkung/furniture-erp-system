package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.math.BigDecimal; // <<< Import BigDecimal

@Entity
@Table(name = "delivery_zones")
public class DeliveryZone {

    @Id
    @Column(name = "zone_id", length = 10)
    private String zoneID;

    public String getZoneID() {
		return zoneID;
	}

	public void setZoneID(String zoneID) {
		this.zoneID = zoneID;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public BigDecimal getDeliveryFee() {
		return deliveryFee;
	}

	public void setDeliveryFee(BigDecimal deliveryFee) {
		this.deliveryFee = deliveryFee;
	}

	@Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee; // <<< แก้เป็น BigDecimal

    // --- Constructor ---
    public DeliveryZone() {
    }

  
}