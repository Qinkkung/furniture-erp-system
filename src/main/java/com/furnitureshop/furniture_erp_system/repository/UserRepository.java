package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furnitureshop.furniture_erp_system.model.User;

/**
 * Repository Interface สำหรับ Product Entity
 * JpaRepository<Product, String> หมายถึง:
 * - จัดการ Entity ชื่อ Product
 * - Primary Key ของ Product เป็นชนิด String (คือ productID)
 */
@Repository // บอก Spring Boot ว่านี่คือ Repository
public interface UserRepository extends JpaRepository<User, String> {
    // --- ไม่ต้องเขียนอะไรเพิ่มเลย! ---
    // JpaRepository มีคำสั่งพื้นฐาน (save, findById, findAll, delete) ให้เราแล้ว
    // ถ้าต้องการคำสั่งค้นหาพิเศษ ค่อยมาเพิ่มทีหลัง
}