package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.Inventory;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.model.Shipment;
import com.furnitureshop.furniture_erp_system.model.User;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.ShipmentRepository;
import com.furnitureshop.furniture_erp_system.repository.UserRepository;
import com.furnitureshop.furniture_erp_system.service.InventoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors; // ถึงแม้จะไม่ได้ใช้โดยตรง แต่ดีที่มีไว้หากต้องการ stream

@Controller
public class HomeController {

    @Autowired private InventoryService inventoryService; 
    @Autowired private SalesOrderRepository salesOrderRepository; 
    @Autowired private PurchaseOrderRepository purchaseOrderRepository; 
    @Autowired private UserRepository userRepository;
    @Autowired private ShipmentRepository shipmentRepository; // <<< ต้องมี ShipmentRepository
    
    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * จัดการคำขอที่เข้ามาที่ URL "/" (หน้าแรก) โดยแยก Dashboard ตาม Role
     */
    @GetMapping("/")
    public String showDashboard(Model model) { 
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // 1. ตรวจสอบ Role
        boolean isSales = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Sales"));
        boolean isStock = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Stock"));
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
        boolean isDelivery = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Delivery"));
            
        // 2. ดึง User Entity สำหรับ Logic ที่ต้องใช้ UserID
        User currentUser = userRepository.findByUsername(username).orElse(null); 
            
        // 3. แยก Logic ตาม Role
        
        if (isSales) {
            // === LOGIC 1: ฝ่ายขาย (Sales) ===
            BigDecimal todaySales = BigDecimal.ZERO;
            List<SalesOrder> awaitingShipment = List.of(); 
            
            if (currentUser != null) {
                 // ใช้ try-catch เผื่อกรณีที่ยังไม่มีข้อมูลในตาราง SO เลย
                 try {
                      todaySales = salesOrderRepository.sumGrandTotalByOrderDateAndUser(LocalDate.now(), currentUser);
                 } catch (Exception e) {
                      todaySales = BigDecimal.ZERO;
                 }
                 awaitingShipment = salesOrderRepository.findByStatusAndUser("Awaiting Shipment", currentUser);
            }
            
            model.addAttribute("todaySales", todaySales != null ? todaySales : BigDecimal.ZERO);
            model.addAttribute("awaitingShipmentCount", awaitingShipment.size());
            
            return "dashboard-sales"; 
            
        } else if (isStock || isAdmin) {
            // === LOGIC 2: ฝ่ายคลัง/แอดมิน (Stock/Admin - Dashboard รวม) ===
            
            List<SalesOrder> awaitingShipment = salesOrderRepository.findByStatus("Awaiting Shipment");
            List<PurchaseOrder> pendingPOs = purchaseOrderRepository.findByStatus("Ordered");
            List<Inventory> lowStockItems = inventoryService.getLowStockItems(LOW_STOCK_THRESHOLD); 
            
            model.addAttribute("awaitingShipmentCount", awaitingShipment.size());
            model.addAttribute("pendingPoCount", pendingPOs.size());
            model.addAttribute("lowStockItems", lowStockItems);

            return "dashboard-stock"; // <<< View ที่เดิมชื่อ dashboard.html
            
        } else if (isDelivery) {
            // === LOGIC 3: ฝ่ายจัดส่ง (Delivery) ===
            
            List<SalesOrder> awaitingOrders = salesOrderRepository.findByStatus("Awaiting Shipment");
            // Shipment ที่สถานะ Pending หรือ Shipped (กำลังขนส่ง)
            List<Shipment> processingShipments = shipmentRepository.findByStatusIn(List.of("Pending", "Shipped")); 
            
            model.addAttribute("awaitingOrdersCount", awaitingOrders.size()); 
            model.addAttribute("processingShipmentCount", processingShipments.size()); 
            
            return "dashboard-delivery"; // <<< View สำหรับฝ่ายจัดส่ง
            
        } else {
             // Fallback
             return "dashboard-stock"; 
        }
    }

    /**
     * จัดการหน้า Login (คงเดิม)
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}