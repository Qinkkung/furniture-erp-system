package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.SalesOrderItem; // Import SalesOrderItem Entity

@Repository // บอก Spring ว่านี่คือ Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Integer> { // จัดการ SalesOrderItem, PK คือ Integer

    // --- ไม่ต้องใส่อะไรข้างใน ---
    // (ถ้าต้องการค้นหา Item ทั้งหมดใน SO เดียว ค่อยมาเพิ่ม List<SalesOrderItem> findBySalesOrder_OrderID(String orderId); ทีหลัง)

}