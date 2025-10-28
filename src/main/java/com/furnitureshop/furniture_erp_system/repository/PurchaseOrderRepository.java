package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder; // Import PurchaseOrder Entity

@Repository // บอก Spring ว่านี่คือ Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> { // จัดการ PurchaseOrder, PK คือ String

    // --- ไม่ต้องใส่อะไรข้างใน ---

}