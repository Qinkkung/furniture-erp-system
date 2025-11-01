package com.furnitureshop.furniture_erp_system.repository;

import com.furnitureshop.furniture_erp_system.model.PosTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosTransactionRepository extends JpaRepository<PosTransaction, String> {
    // (ไม่ต้องใส่อะไรเพิ่ม JpaRepository มีคำสั่งพื้นฐานให้แล้ว)
}