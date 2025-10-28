package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furnitureshop.furniture_erp_system.model.Shipment;
import java.util.List;

@Repository // บอก Spring ว่านี่คือ Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> { // จัดการ Supplier, PK คือ String

	List<Shipment> findByStatusIn(List<String> statuses);

}