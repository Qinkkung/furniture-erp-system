package com.furnitureshop.furniture_erp_system.repository;

import com.furnitureshop.furniture_erp_system.model.ProductVariant; // <<< เพิ่ม
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrderItem; // <<< เพิ่ม
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    // (Method ที่เราสร้างไว้สำหรับ DataInitializer)
    // (มันถูกย้ายไป PurchaseOrderItemRepository แล้ว ไม่เป็นไร)

    /**
     * ค้นหา PO ตาม ID พร้อมดึง Items และ Supplier (สำหรับหน้า Receive/QC)
     */
    @Query("SELECT po FROM PurchaseOrder po " +
           "LEFT JOIN FETCH po.supplier s " +
           "LEFT JOIN FETCH po.purchaseOrderItems poi " +
           "LEFT JOIN FETCH poi.productVariant pv " +
           "WHERE po.poID = :id")
    Optional<PurchaseOrder> findByIdWithItems(@Param("id") String id);

    /**
     * *** เพิ่ม Method นี้ ***
     * ค้นหา PO ทั้งหมด พร้อมดึงข้อมูล Supplier มาด้วย (สำหรับหน้ารายการ PO)
     * และเรียงลำดับตามวันที่สั่ง
     */
    @Query("SELECT po FROM PurchaseOrder po " +
           "LEFT JOIN FETCH po.supplier s " + // <<< บังคับดึง Supplier
           "ORDER BY po.orderDate DESC") // <<< เรียงลำดับ (Optional)
    List<PurchaseOrder> findAllWithSupplier(); // <<< ตั้งชื่อใหม่

}