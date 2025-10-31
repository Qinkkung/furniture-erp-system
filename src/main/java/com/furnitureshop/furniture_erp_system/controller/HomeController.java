package com.furnitureshop.furniture_erp_system.controller;

// --- Imports ---
import com.furnitureshop.furniture_erp_system.model.Inventory;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository;
import com.furnitureshop.furniture_erp_system.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // <<< Import Model
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List; // <<< Import List
import java.util.stream.Collectors; // <<< Import Collectors (ถ้าใช้)

/**
 * Controller สำหรับจัดการคำขอทั่วไปเกี่ยวกับหน้าบ้าน/dashboard
 */
@Controller
public class HomeController {

    // --- ฉีด Services และ Repositories ที่จำเป็น ---
    @Autowired
    private InventoryService inventoryService; //

    @Autowired
    private SalesOrderRepository salesOrderRepository; //

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository; //
    
    // กำหนดค่าคงที่สำหรับสินค้าใกล้หมด
    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * จัดการคำขอที่เข้ามาที่ URL "/" (หน้าแรก) และแสดงหน้า dashboard
     * (*** อัปเกรดให้ดึงข้อมูล Logic ***)
     */
    @GetMapping("/")
    public String showDashboard(Model model) { // <<< เพิ่ม Model
        
        // --- 1. คำนวณ: ดึง SO ที่รอจัดส่ง ---
        // (เรียก Method ที่เราสร้างไว้ใน SalesOrderRepository)
        List<SalesOrder> awaitingShipment = salesOrderRepository.findByStatus("Awaiting Shipment"); //
        
        // --- 2. คำนวณ: ดึง PO ที่รอรับของ ---
        // (เรียก Method ที่เราสร้างไว้ใน PurchaseOrderRepository)
        List<PurchaseOrder> pendingPOs = purchaseOrderRepository.findByStatus("Ordered"); //

        // --- 3. คำนวณ: ดึงสินค้าใกล้หมด (ATS < 10) ---
        // (เรียก Method ที่เราสร้างไว้ใน InventoryService)
        List<Inventory> lowStockItems = inventoryService.getLowStockItems(LOW_STOCK_THRESHOLD); //

        // --- (Optional) 4. คำนวณ: มูลค่าสต็อก ---
        // (Logic นี้ซับซ้อน ต้องสร้างเพิ่มใน Service - ข้ามไปก่อนได้)
        // BigDecimal totalValue = inventoryService.getTotalInventoryValue();

        // --- 5. ส่งข้อมูลทั้งหมดไปให้ View (HTML) ---
        
        // *** นี่คือบรรทัดที่แก้ไข (บรรทัด 58 ของคุณ) ***
        model.addAttribute("awaitingShipmentCount", awaitingShipment.size()); // <<< แก้จาก awaitingOrders เป็น awaitingShipment
        
        model.addAttribute("pendingPoCount", pendingPOs.size()); // ส่งแค่ "จำนวน"
        model.addAttribute("lowStockItems", lowStockItems); // ส่ง "List" ของสินค้าใกล้หมดไปเลย
        // model.addAttribute("totalStockValue", totalValue); // (ถ้าทำ)

        return "dashboard"; // อ้างอิงถึงไฟล์ "dashboard.html"
    }

    /**
     * === เพิ่ม Method นี้ ===
     * จัดการหน้า Login
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // คืนค่าไฟล์ "login.html"
    }
}