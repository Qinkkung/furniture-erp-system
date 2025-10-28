package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.model.Shipment;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository; // ใช้ค้นหา SO ที่พร้อมส่ง
import com.furnitureshop.furniture_erp_system.repository.ShipmentRepository; // ใช้ค้นหา Shipment
import com.furnitureshop.furniture_erp_system.service.ShipmentService; // Import ShipmentService

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/shipments") // URL หลักสำหรับ Shipment
public class ShipmentController {

    @Autowired private ShipmentService shipmentService;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private ShipmentRepository shipmentRepository; // เพิ่ม Repo Shipment

    /**
     * แสดงหน้ารายการงานจัดส่ง (แบ่งเป็น 2 ส่วน: รอสร้าง Shipment และ กำลังดำเนินการ)
     */
    @GetMapping("")
    public String listShipments(Model model) {
        // 1. ดึง SO ที่มีสถานะ 'Awaiting Shipment'
        List<SalesOrder> awaitingOrders = salesOrderRepository.findByStatus("Awaiting Shipment");

        // 2. ดึง Shipment ที่มีสถานะ 'Pending' หรือ 'Shipped'
        List<Shipment> processingShipments = shipmentRepository.findByStatusIn(List.of("Pending", "Shipped"));

        model.addAttribute("awaitingOrders", awaitingOrders);
        model.addAttribute("processingShipments", processingShipments);

        return "shipment-list"; // ต้องสร้างไฟล์ templates/shipment-list.html
    }

    /**
     * จัดการการสร้าง Shipment จาก Sales Order ที่เลือก
     */
    @PostMapping("/create")
    public String createShipmentFromOrder(@RequestParam("orderId") String orderId,
                                          RedirectAttributes redirectAttributes) {
        try {
            // เรียก Service สร้าง Shipment (ส่ง null สำหรับ shipDate ไปก่อน)
            Shipment createdShipment = shipmentService.createShipment(orderId, null);
            redirectAttributes.addFlashAttribute("successMessage", "สร้าง Shipment '" + createdShipment.getShipmentID() + "' สำหรับ Order '" + orderId + "' สำเร็จ!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการสร้าง Shipment: " + e.getMessage());
        }
        return "redirect:/shipments";
    }

    /**
     * จัดการการอัปเดตสถานะ Shipment (เช่น กดปุ่ม "จัดส่งแล้ว")
     */
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("shipmentId") String shipmentId,
                               @RequestParam("newStatus") String newStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            // เรียก Service อัปเดตสถานะ (และตัดสต็อกถ้าเป็น 'Delivered')
            Shipment updatedShipment = shipmentService.updateShipmentStatus(shipmentId, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "อัปเดตสถานะ Shipment '" + shipmentId + "' เป็น '" + newStatus + "' สำเร็จ!");
             // แจ้งเตือนพิเศษ ถ้าเป็นการตัดสต็อก
            if ("Delivered".equals(newStatus)) {
                 redirectAttributes.addFlashAttribute("infoMessage", "สต็อกสินค้าถูกตัดออกจากระบบแล้ว");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการอัปเดตสถานะ: " + e.getMessage());
        }
        return "redirect:/shipments";
    }
}