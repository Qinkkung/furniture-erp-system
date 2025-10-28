package com.furnitureshop.furniture_erp_system.repository;

import java.util.List; // ต้อง import List
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.furnitureshop.furniture_erp_system.model.Payment; // ต้อง import Payment

@Repository // ต้องใส่ @Repository
public interface PaymentRepository extends JpaRepository<Payment, String> { // ต้อง extends JpaRepository

    // Method นี้ถูกต้องแล้ว
    List<Payment> findBySalesOrder_OrderID(String orderId);
}