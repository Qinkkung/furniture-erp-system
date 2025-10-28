package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.DeliveryZone; // Import DeliveryZone Entity

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, String> { // จัดการ DeliveryZone, PK คือ String

    // --- ไม่ต้องใส่อะไรข้างใน ---

}