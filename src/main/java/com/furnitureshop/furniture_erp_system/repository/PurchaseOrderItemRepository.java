package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrderItem; // Import PurchaseOrderItem Entity

@Repository // บอก Spring ว่านี่คือ Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Integer> { // จัดการ PurchaseOrderItem, PK คือ Integer

    // --- ไม่ต้องใส่อะไรข้างใน ---

}