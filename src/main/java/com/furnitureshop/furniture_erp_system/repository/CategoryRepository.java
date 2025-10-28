package com.furnitureshop.furniture_erp_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furnitureshop.furniture_erp_system.model.Category; // Import Category Entity

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    // ตอนนี้ยังไม่ต้องใส่อะไรเพิ่ม JpaRepository มีคำสั่งพื้นฐานให้แล้ว
}