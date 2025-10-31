package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.User;
import java.util.Optional; // <<< Import ที่เพิ่มเข้ามา

/**
 * Repository Interface สำหรับ Product Entity
 * JpaRepository<Product, String> หมายถึง:
 * - จัดการ Entity ชื่อ Product
 * - Primary Key ของ Product เป็นชนิด String (คือ productID)
 */
@Repository // บอก Spring Boot ว่านี่คือ Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // === เพิ่ม Method นี้ ===
    // Spring Data JPA จะสร้าง Query ให้เราอัตโนมัติ
    // เพื่อค้นหา User จาก field ที่ชื่อ "username"
    Optional<User> findByUsername(String username);

}