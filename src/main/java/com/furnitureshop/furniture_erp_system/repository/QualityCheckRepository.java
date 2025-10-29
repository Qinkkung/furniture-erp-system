package com.furnitureshop.furniture_erp_system.repository;

import com.furnitureshop.furniture_erp_system.model.QualityCheck; // Import QualityCheck Entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Import List (if using the custom finder)

/**
 * Repository Interface สำหรับ QualityCheck Entity
 * JpaRepository<QualityCheck, Integer> หมายถึง:
 * - จัดการ Entity ชื่อ QualityCheck
 * - Primary Key ของ QualityCheck เป็นชนิด Integer (คือ qcID)
 */
@Repository // บอก Spring Boot ว่านี่คือ Repository
public interface QualityCheckRepository extends JpaRepository<QualityCheck, Integer> {

    // --- Optional: Method ค้นหา QC ตาม PO Item ID ---
    /**
     * ค้นหา QualityCheck records ทั้งหมดที่เชื่อมโยงกับ PurchaseOrderItem ID ที่กำหนด
     * Spring Data JPA จะสร้าง query ให้จากชื่อ method
     * @param poItemId ID ของ PurchaseOrderItem
     * @return List ของ QualityCheck ที่ตรงกัน (อาจจะว่าง)
     */
    // List<QualityCheck> findByPurchaseOrderItem_PoItemID(Integer poItemId);

    // --- ไม่ต้องเขียนอะไรเพิ่ม ถ้ายังไม่ต้องการ Method ค้นหาพิเศษ ---
    // JpaRepository มีคำสั่งพื้นฐาน (save, findById, findAll, delete) ให้แล้ว
}