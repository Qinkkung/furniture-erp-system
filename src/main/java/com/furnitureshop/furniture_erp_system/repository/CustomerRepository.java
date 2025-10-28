package com.furnitureshop.furniture_erp_system.repository;

// --- Imports ที่จำเป็น ---
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.Customer; // Import Customer Entity

// --- Annotation และ extends ---
@Repository // ต้องใส่ @Repository เพื่อบอก Spring
public interface CustomerRepository extends JpaRepository<Customer, String> { // ต้อง extends JpaRepository<Entity, ID_Type>

    // --- ตอนนี้ยังไม่ต้องใส่อะไรข้างใน ---
    // (เว้นแต่ต้องการคำสั่งค้นหาพิเศษ)

}