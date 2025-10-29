package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrderItem;
import com.furnitureshop.furniture_erp_system.model.QualityCheck; // <<< Import QualityCheck
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderItemRepository; // <<< Import PO Item Repo
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.QualityCheckRepository; // <<< Import QC Repo (ต้องสร้าง)
import com.furnitureshop.furniture_erp_system.service.InventoryService; // <<< Import InventoryService
import com.furnitureshop.furniture_erp_system.service.PurchaseOrderService; // <<< Import PO Service

import jakarta.servlet.http.HttpServletRequest; // <<< Import HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/stock") // URL หลักสำหรับสต็อก
public class StockController {

    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseOrderItemRepository purchaseOrderItemRepository; // ใช้ดึง Item
    @Autowired private QualityCheckRepository qualityCheckRepository; // ใช้บันทึกผล QC
    @Autowired private InventoryService inventoryService; // ใช้เพิ่มสต็อก
    @Autowired private PurchaseOrderService purchaseOrderService; // ใช้อัปเดตสถานะ PO

    /**
     * แสดงหน้าฟอร์มรับสินค้า / QC สำหรับ PO ที่กำหนด
     * @param poId รหัส PO (จาก URL)
     * @param model
     * @return ชื่อไฟล์ HTML (po-intake-form.html)
     */
    @GetMapping("/receive/{poId}")
    public String showPoIntakeForm(@PathVariable("poId") String poId, Model model, RedirectAttributes redirectAttributes) {
        // 1. ดึงข้อมูล PO พร้อม Items (อาจต้องใช้ JOIN FETCH ถ้า Lazy)
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findById(poId); // <<< อาจจะต้องแก้เป็น findByIdWithItems

        if (poOpt.isEmpty() || !"Ordered".equals(poOpt.get().getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ PO หรือ PO นี้ไม่อยู่ในสถานะ 'Ordered'");
            return "redirect:/purchase-orders"; // กลับหน้ารายการ PO
        }

        PurchaseOrder po = poOpt.get();
        model.addAttribute("purchaseOrder", po);
        // (Optional) อาจจะส่ง List<String> ของ QC Status ไปให้ View ใช้ใน Dropdown

        return "po-intake-form"; // ต้องสร้างไฟล์ templates/po-intake-form.html
    }

    /**
     * จัดการการ submit ฟอร์มรับสินค้า / QC
     * @param poId รหัส PO
     * @param request HttpServletRequest เพื่อดึงข้อมูลฟอร์มแบบไดนามิก
     * @param redirectAttributes
     * @return Redirect กลับไปหน้ารายการ PO
     */
    @PostMapping("/receive/save/{poId}")
    public String processPoIntake(@PathVariable("poId") String poId,
                                  HttpServletRequest request, // <<< ใช้ HttpServletRequest
                                  RedirectAttributes redirectAttributes) {

        // 1. ดึงข้อมูล PO อีกครั้งเพื่อความปลอดภัย
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findById(poId);
        if (poOpt.isEmpty() || !"Ordered".equals(poOpt.get().getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "PO ไม่ถูกต้อง หรือถูกรับไปแล้ว");
            return "redirect:/purchase-orders";
        }
        PurchaseOrder po = poOpt.get();

        // 2. ดึงข้อมูลจากฟอร์ม (Quantity Received และ QC Status สำหรับแต่ละ Item)
        //    เราจะใช้ HttpServletRequest เพราะชื่อ input field มันไดนามิก
        Map<String, String[]> formParameters = request.getParameterMap();
        List<String> errors = new ArrayList<>();
        boolean allReceived = true; // ตั้งธงว่ารับครบทุกรายการหรือไม่

        // 3. วนลูป處理แต่ละ PO Item ที่ถูกส่งมาจากฟอร์ม
        for (PurchaseOrderItem item : po.getPurchaseOrderItems()) {
            Integer poItemId = item.getPoItemID(); // ID ของ PO Item
            // ดึงค่า quantity_received และ qc_status จากชื่อ input ที่เราตั้ง (เช่น "qty_101", "qc_101")
            String qtyReceivedStr = request.getParameter("qty_" + poItemId);
            String qcStatus = request.getParameter("qc_" + poItemId);

            int quantityReceived = 0;
            try {
                if (qtyReceivedStr != null && !qtyReceivedStr.isBlank()) {
                    quantityReceived = Integer.parseInt(qtyReceivedStr);
                    if (quantityReceived < 0) throw new NumberFormatException("Quantity cannot be negative");
                }
                // ถ้าไม่กรอกจำนวนมาเลย ถือว่าเป็น 0
            } catch (NumberFormatException e) {
                errors.add("จำนวนที่รับสำหรับ Item ID " + poItemId + " ไม่ถูกต้อง");
                continue; // ข้ามไป Item ถัดไป
            }

            // --- Logic การบันทึก QC และเพิ่มสต็อก ---
            if (quantityReceived > 0 && qcStatus != null && !qcStatus.isBlank()) {
                // (Optional) ตรวจสอบว่าเคยบันทึก QC ของ Item นี้ไปแล้วหรือยัง (ป้องกันการบันทึกซ้ำ)
                // List<QualityCheck> existingQCs = qualityCheckRepository.findByPurchaseOrderItem_PoItemID(poItemId);
                // if (!existingQCs.isEmpty()) continue; // ถ้าเคย QC แล้ว ข้ามไป

                // 1. บันทึกผล QC
                QualityCheck qc = new QualityCheck();
                qc.setPurchaseOrderItem(item);
                qc.setStatus(qcStatus);
                qc.setNotes(request.getParameter("notes_" + poItemId)); // รับค่า Notes (ถ้ามี)
                qualityCheckRepository.save(qc);

                // 2. เพิ่มสต็อก ถ้า QC ผ่าน (Pass หรือ B-Grade)
                if ("Pass".equals(qcStatus) || "B-Grade".equals(qcStatus)) {
                    try {
                        inventoryService.addStockOnHand(item.getProductVariant().getVariantID(), quantityReceived);
                         // (Optional) อาจจะติด Flag 'B-Grade' ใน Inventory ถ้าต้องการ
                    } catch (RuntimeException e) {
                        errors.add("เกิดข้อผิดพลาดในการเพิ่มสต็อกสำหรับ Item ID " + poItemId + ": " + e.getMessage());
                    }
                }
            }

            // เช็คว่ารับครบตามจำนวนที่สั่งหรือไม่
            if (quantityReceived < item.getQuantity()) {
                allReceived = false; // มีบางรายการที่รับไม่ครบ
            }
        } // จบ Loop for

        // 4. อัปเดตสถานะ PO
        if (!errors.isEmpty()) {
            // ถ้ามี Error ให้แจ้งเตือน แต่ PO อาจจะยังเป็น Ordered อยู่ (ถ้ายังรับไม่ครบ)
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดบางรายการ: " + String.join(", ", errors));
             // ถ้าต้องการให้อัปเดตสถานะแม้มี Error (เช่น เป็น Partial Received) ก็ทำตรงนี้
        } else {
            // ถ้าไม่มี Error
            String finalPoStatus = allReceived ? "Received" : "Partially Received"; // <<< (Optional) เพิ่มสถานะรับบางส่วน
            purchaseOrderService.updatePurchaseOrderStatus(poId, finalPoStatus);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกการรับสินค้าสำหรับ PO '" + poId + "' สำเร็จ! สถานะ: " + finalPoStatus);
            if (finalPoStatus.equals("Received")) {
                 redirectAttributes.addFlashAttribute("infoMessage", "สต็อกสินค้าถูกเพิ่มเข้าระบบแล้ว (สำหรับรายการที่ QC ผ่าน)");
            }
        }

        return "redirect:/purchase-orders";
    }

}