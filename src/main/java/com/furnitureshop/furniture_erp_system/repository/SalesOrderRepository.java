package com.furnitureshop.furniture_erp_system.repository;

import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <<< Import Query
import org.springframework.data.repository.query.Param; // <<< Import Param
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // <<< Import Optional

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, String> {

    List<SalesOrder> findByStatus(String status);

    List<SalesOrder> findByCustomer_CustomerID(String customerId); // (เพิ่มถ้าต้องการ)

    // *** ตรวจสอบ Query นี้ให้ถูกต้อง ***
    @Query("SELECT so FROM SalesOrder so " +
           "LEFT JOIN FETCH so.customer c " +
           "LEFT JOIN FETCH c.deliveryZone dz " +
           "LEFT JOIN FETCH so.user u " +
           "LEFT JOIN FETCH so.salesOrderItems soi " +
           "LEFT JOIN FETCH soi.productVariant pv " + // <<< ต้องมี Fetch Variant
        // "LEFT JOIN FETCH pv.product p " + // (Optional - อาจไม่จำเป็นสำหรับหน้านี้)
           "LEFT JOIN FETCH so.payments pay " +
           "WHERE so.orderID = :id")
    Optional<SalesOrder> findByIdWithDetails(@Param("id") String id);

}