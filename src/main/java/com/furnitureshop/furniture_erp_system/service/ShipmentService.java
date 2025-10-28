package com.furnitureshop.furniture_erp_system.service;

import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.model.SalesOrderItem;
import com.furnitureshop.furniture_erp_system.model.Shipment;
// --- Import Repositories ที่จำเป็น ---
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.ShipmentRepository; // ต้องสร้าง Repo นี้ด้วย

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ShipmentService {

    @Autowired
    private ShipmentRepository shipmentRepository; // Repo สำหรับ Shipment

    @Autowired
    private SalesOrderRepository salesOrderRepository; // Repo สำหรับ SO

    @Autowired
    private InventoryService inventoryService; // <<< เรียกใช้ InventoryService เพื่อตัดสต็อก

    // --- Logic การสร้าง Shipment ---
    /**
     * สร้าง Shipment ใหม่จาก Sales Order ที่พร้อมส่ง
     * @param orderId รหัส SO ที่จะสร้าง Shipment ให้
     * @param shipDate วันที่กำหนดส่ง (ถ้ามี)
     * @return Shipment ที่สร้างเสร็จ
     * @throws RuntimeException ถ้า SO ไม่เจอ หรือ สถานะไม่ถูกต้อง
     */
    @Transactional
    public Shipment createShipment(String orderId, LocalDate shipDate) {
        // 1. ค้นหา SalesOrder
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found: " + orderId));

        // 2. ตรวจสอบสถานะ SO (ต้องเป็น 'Awaiting Shipment')
        if (!"Awaiting Shipment".equals(order.getStatus())) {
            throw new RuntimeException("SalesOrder is not ready for shipment. Status: " + order.getStatus());
        }

        // 3. สร้าง Shipment
        Shipment shipment = new Shipment();
        shipment.setShipmentID(generateShipmentId()); // ควรมีฟังก์ชันสร้าง ID
        shipment.setSalesOrder(order);
        shipment.setShipDate(shipDate); // อาจจะ null ได้ถ้ายังไม่กำหนด
        shipment.setStatus("Pending"); // สถานะเริ่มต้น

        // 4. (สำคัญ) อัปเดตสถานะ SO เป็น 'Processing Shipment' หรือ 'Shipped' (แล้วแต่ Workflow)
        order.setStatus("Processing Shipment"); // หรือ Shipped เลยก็ได้
        salesOrderRepository.save(order);

        // 5. บันทึก Shipment
        return shipmentRepository.save(shipment);
    }

    // --- Logic การอัปเดตสถานะ Shipment (และตัดสต็อกเมื่อ Delivered) ---
    /**
     * อัปเดตสถานะ Shipment และทำการตัดสต็อกจริงเมื่อสถานะเป็น 'Delivered'
     * @param shipmentId รหัส Shipment
     * @param newStatus สถานะใหม่ (Pending, Shipped, Delivered)
     * @return Shipment ที่อัปเดตแล้ว
     * @throws RuntimeException ถ้า Shipment ไม่เจอ
     */
    @Transactional
    public Shipment updateShipmentStatus(String shipmentId, String newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));

        String oldStatus = shipment.getStatus();
        shipment.setStatus(newStatus);

        // *** (Logic การตัดสต็อกจริง - หัวใจสำคัญ) ***
        if ("Delivered".equals(newStatus) && !"Delivered".equals(oldStatus)) {
            // ดึงรายการ Items จาก SO ที่ผูกกับ Shipment นี้
            SalesOrder order = shipment.getSalesOrder();
            Set<SalesOrderItem> items = order.getSalesOrderItems();

            // วนลูปตัดสต็อกทีละ Item
            for (SalesOrderItem item : items) {
                inventoryService.deductStock(item.getProductVariant().getVariantID(), item.getQuantity());
            }

            // (Optional) อัปเดตสถานะ SO เป็น 'Completed'
            order.setStatus("Completed");
            salesOrderRepository.save(order);
        }

        return shipmentRepository.save(shipment);
    }

    // --- Helper Method ---
    private String generateShipmentId() {
        // Logic สร้าง Shipment ID
        return "SH-" + System.currentTimeMillis();
    }
}