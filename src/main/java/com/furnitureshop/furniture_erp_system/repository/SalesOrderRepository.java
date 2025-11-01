package com.furnitureshop.furniture_erp_system.repository;

import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, String> {

    // Method เดิม
    List<SalesOrder> findByStatus(String status);
    
    // === Methods สำหรับ Dashboard Sales ===
    List<SalesOrder> findByStatusAndUser(String status, User user);
    
    @Query("SELECT SUM(so.grandTotal) FROM SalesOrder so WHERE so.orderDate = :date AND so.user = :user")
    BigDecimal sumGrandTotalByOrderDateAndUser(@Param("date") LocalDate date, @Param("user") User user);
    
    // === Method ที่ขาดหายไป (Fix Error ใน Controller) ===
    /**
     * ค้นหา SO ตาม ID พร้อมดึง Items, Customer, User, Payments เพื่อป้องกัน Lazy Loading
     */
    @Query("SELECT so FROM SalesOrder so " +
           "LEFT JOIN FETCH so.customer c " +
           "LEFT JOIN FETCH c.deliveryZone dz " +
           "LEFT JOIN FETCH so.user u " +
           "LEFT JOIN FETCH so.salesOrderItems soi " +
           "LEFT JOIN FETCH soi.productVariant pv " + 
           "LEFT JOIN FETCH so.payments pay " +
           "WHERE so.orderID = :id")
    Optional<SalesOrder> findByIdWithDetails(@Param("id") String id); // <<< นี่คือตัวที่แก้ Error
}