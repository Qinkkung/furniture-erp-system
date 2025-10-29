package com.furnitureshop.furniture_erp_system.repository;

import com.furnitureshop.furniture_erp_system.model.PurchaseOrder; // Import PurchaseOrder Entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <<< Import Query
import org.springframework.data.repository.query.Param; // <<< Import Param
import org.springframework.stereotype.Repository;

import java.util.List; // Import List (เผื่อใช้ Method อื่น)
import java.util.Optional; // <<< Import Optional

@Repository // บอก Spring ว่านี่คือ Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> { // จัดการ PurchaseOrder, PK คือ String

    // --- Method พื้นฐานมีให้แล้วโดย JpaRepository ---
    // (save, findById, findAll, delete)

    // --- (Optional) Method ค้นหาพิเศษ (ถ้าต้องการ) ---
    // List<PurchaseOrder> findByStatus(String status);
    // List<PurchaseOrder> findBySupplier_SupplierID(String supplierId);

    // --- Method ใหม่สำหรับดึง PO พร้อม Items (สำหรับหน้า Receive/QC) ---
    /**
     * ค้นหา PurchaseOrder ตาม ID พร้อมดึงข้อมูล Supplier และ Items (PurchaseOrderItems)
     * รวมถึง ProductVariant ที่อยู่ใน Items มาด้วยเลย (แก้ปัญหา Lazy Loading)
     * @param id รหัส PO ที่ต้องการค้นหา
     * @return Optional<PurchaseOrder> ที่มีข้อมูลครบถ้วน หรือ Optional ว่างถ้าหาไม่เจอ
     */
    @Query("SELECT po FROM PurchaseOrder po " +
           "LEFT JOIN FETCH po.supplier s " + // ดึง Supplier
           "LEFT JOIN FETCH po.purchaseOrderItems poi " + // ดึง Items
           "LEFT JOIN FETCH poi.productVariant pv " + // ดึง Variant ของ Item
           // "LEFT JOIN FETCH pv.product p " + // (Optional) ดึง Product ของ Variant (ถ้าต้องการ)
           "WHERE po.poID = :id")
    Optional<PurchaseOrder> findByIdWithItems(@Param("id") String id);

}