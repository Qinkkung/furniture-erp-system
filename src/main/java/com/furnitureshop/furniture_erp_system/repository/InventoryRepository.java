package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furnitureshop.furniture_erp_system.model.Inventory;
import java.util.Optional; // ต้อง Import Optional

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    /**
     * ค้นหาแถว Inventory ที่ผูกกับ ProductVariant ID ที่กำหนด
     * Spring Data JPA จะสร้าง SQL query ให้เราอัตโนมัติจากชื่อ method นี้:
     * findBy = คำนำหน้า บอกว่าเป็นการค้นหา
     * ProductVariant = ให้มองเข้าไปในฟิลด์ชื่อ 'productVariant' ของ Inventory entity
     * _ = ตัวคั่น สำหรับการเข้าถึง property ที่ซ้อนกัน
     * VariantID = ให้มองหาฟิลด์ชื่อ 'variantID' ที่อยู่ใน object 'productVariant'
     *
     * @param variantId คือ ID ของ ProductVariant ที่ต้องการค้นหา
     * @return Optional<Inventory> ถ้าเจอ Inventory ก็จะส่งค่ากลับมา ถ้าไม่เจอก็ส่งค่าว่าง
     */
    Optional<Inventory> findByProductVariant_VariantID(String variantId);

    // ไม่ต้องเขียนโค้ดข้างใน! Spring ทำให้เราเอง
}