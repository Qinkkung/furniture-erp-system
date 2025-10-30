package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrderItem;
import com.furnitureshop.furniture_erp_system.model.QualityCheck;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderItemRepository;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.QualityCheckRepository;
import com.furnitureshop.furniture_erp_system.service.InventoryService;
import com.furnitureshop.furniture_erp_system.service.PurchaseOrderService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/stock") // URL หลักสำหรับสต็อก
public class StockController {

    // Logger สำหรับ Debug
    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseOrderItemRepository purchaseOrderItemRepository; // ใช้ดึง Item (อาจไม่จำเป็นถ้าใช้ Fetch)
    @Autowired private QualityCheckRepository qualityCheckRepository;
    @Autowired private InventoryService inventoryService;
    @Autowired private PurchaseOrderService purchaseOrderService;

    /**
     * แสดงหน้าฟอร์มรับสินค้า / QC สำหรับ PO ที่กำหนด
     */
    @GetMapping("/receive/{poId}")
    public String showPoIntakeForm(@PathVariable("poId") String poId, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Accessing receive form for PO ID: {}", poId); // <<< Log

        // *** แก้ไขจุดที่ 1: ใช้ findByIdWithItems ***
        // ดึงข้อมูล PO พร้อม Items, Supplier, Variant (ป้องกัน Lazy Loading)
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findByIdWithItems(poId); //

        if (poOpt.isEmpty()) {
             logger.warn("PO ID: {} not found.", poId); // <<< Log
             redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Purchase Order ID: " + poId);
             return "redirect:/purchase-orders";
        }

        PurchaseOrder po = poOpt.get();

        // ตรวจสอบสถานะ PO
        if (!"Ordered".equals(po.getStatus()) && !"Partially Received".equals(po.getStatus())) { // <<< อนุญาต Partially Received ด้วย
             logger.warn("PO ID: {} has invalid status: {}", poId, po.getStatus()); // <<< Log
             redirectAttributes.addFlashAttribute("errorMessage", "PO นี้ไม่อยู่ในสถานะที่สามารถรับสินค้าได้ (สถานะปัจจุบัน: " + po.getStatus() + ")");
             return "redirect:/purchase-orders";
        }

        model.addAttribute("purchaseOrder", po);
        
        // (Optional: ดึง QC records เก่ามาแสดงผลด้วย ถ้าต้องการ)
        // Map<Integer, QualityCheck> existingQcs = ...
        // model.addAttribute("existingQcs", existingQcs);

        logger.info("Showing receive form for PO ID: {}", poId); // <<< Log
        return "po-intake-form"; //
    }

    /**
     * จัดการการ submit ฟอร์มรับสินค้า / QC
     */
    @PostMapping("/receive/save/{poId}")
    public String processPoIntake(@PathVariable("poId") String poId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        logger.info("Processing intake form submission for PO ID: {}", poId); // <<< Log

        // *** แก้ไขจุดที่ 2: ใช้ findByIdWithItems ***
        // ดึง PO พร้อม Items อีกครั้ง เพื่อให้แน่ใจว่าได้ข้อมูลครบถ้วน
        Optional<PurchaseOrder> poOpt = purchaseOrderRepository.findByIdWithItems(poId); //
        
        if (poOpt.isEmpty() || (!"Ordered".equals(poOpt.get().getStatus()) && !"Partially Received".equals(poOpt.get().getStatus())) ) {
            redirectAttributes.addFlashAttribute("errorMessage", "PO ไม่ถูกต้อง หรือสถานะมีการเปลี่ยนแปลง");
            return "redirect:/purchase-orders";
        }
        PurchaseOrder po = poOpt.get();
        Set<PurchaseOrderItem> itemsInPo = po.getPurchaseOrderItems(); // <<< ดึง Items จาก PO ที่ Fetch มา

        List<String> errors = new ArrayList<>();
        List<String> successMessages = new ArrayList<>();
        int itemsSuccessfullyProcessed = 0;
        int totalItemsInPo = itemsInPo.size();
        boolean somethingWasReceived = false; // <<< เพิ่ม Flag เช็คว่ามีการรับของบ้างไหม
        boolean allReceivedCompletely = true; // <<< Flag เช็คว่ารับครบทุกจำนวนที่สั่งหรือไม่

        // วนลูป PO Items ที่มีอยู่ใน PO จริงๆ
        for (PurchaseOrderItem item : itemsInPo) {
            Integer poItemId = item.getPoItemID();
            String variantId = item.getProductVariant().getVariantID(); //
            logger.debug("Processing PO Item ID: {}, Variant ID: {}", poItemId, variantId);

            String qtyReceivedStr = request.getParameter("qty_" + poItemId);
            String qcStatus = request.getParameter("qc_" + poItemId);
            String qcNotes = request.getParameter("notes_" + poItemId);

            logger.debug("Received Qty: '{}', Status: '{}', Notes: '{}'", qtyReceivedStr, qcStatus, qcNotes);

            int quantityReceived = 0;
            try {
                if (qtyReceivedStr != null && !qtyReceivedStr.isBlank()) {
                    quantityReceived = Integer.parseInt(qtyReceivedStr);
                    if (quantityReceived < 0) throw new NumberFormatException("Quantity cannot be negative");
                }
            } catch (NumberFormatException e) {
                String errorMsg = "จำนวนที่รับสำหรับ Item ID " + poItemId + " ไม่ถูกต้อง";
                logger.warn(errorMsg); errors.add(errorMsg);
                allReceivedCompletely = false; // <<< ถือว่ารับไม่ครบถ้า Error
                continue; // ข้ามไป Item ถัดไป
            }

            // ถ้ากรอกจำนวน > 0 และ เลือกสถานะ QC มา
            if (quantityReceived > 0 && qcStatus != null && !qcStatus.isBlank()) {
                 logger.info("Processing QC for Item ID: {}, Qty: {}, Status: {}", poItemId, quantityReceived, qcStatus);
                 somethingWasReceived = true; // <<< ตั้ง Flag ว่ามีการรับของ

                // (Optional) ตรวจสอบว่าเคย QC รายการนี้ไปหรือยัง (อาจจะซับซ้อนถ้ารองรับการรับหลายครั้ง)
                // ...

                // 1. บันทึกผล QC
                QualityCheck qc = new QualityCheck();
                qc.setPurchaseOrderItem(item); //
                qc.setStatus(qcStatus);
                qc.setNotes(qcNotes);
                try {
                    qualityCheckRepository.save(qc); //
                    logger.info("QC record saved for Item ID: {}", poItemId);

                    // 2. เพิ่มสต็อก ถ้า QC ผ่าน (Pass หรือ B-Grade)
                    if ("Pass".equals(qcStatus) || "B-Grade".equals(qcStatus)) {
                        logger.info("Adding stock for Variant ID: {}, Qty: {}", variantId, quantityReceived);
                        inventoryService.addStockOnHand(variantId, quantityReceived); //
                        successMessages.add("เพิ่มสต็อก " + item.getProductVariant().getSkuCode() + " จำนวน " + quantityReceived);
                    } else if ("Fail".equals(qcStatus)) {
                         logger.warn("QC Failed for Item ID: {}", poItemId);
                         successMessages.add("บันทึกผล QC 'Fail' สำหรับ " + item.getProductVariant().getSkuCode());
                    }
                    itemsSuccessfullyProcessed++;

                } catch (Exception e) {
                     String errorMsg = "เกิดข้อผิดพลาดในการบันทึก QC/สต็อกสำหรับ Item ID " + poItemId + ": " + e.getMessage();
                     logger.error(errorMsg, e); errors.add(errorMsg);
                     allReceivedCompletely = false; // <<< ถือว่ารับไม่ครบถ้า Error
                }

            } else if (quantityReceived == 0 && (qcStatus == null || qcStatus.isBlank())) {
                // กรณีไม่กรอกอะไรเลย (Qty=0, Status=ว่าง) ถือว่า "ยังไม่รับ" Item นี้
                 logger.debug("Skipping Item ID: {} (Qty 0 or No Status)", poItemId);
                 itemsSuccessfullyProcessed++;
                 allReceivedCompletely = false; // <<< ถือว่ายังรับไม่ครบ
            } else if (quantityReceived > 0 && (qcStatus == null || qcStatus.isBlank())) {
                 // กรอกจำนวน แต่ไม่เลือกผล QC
                 String errorMsg = "ข้อมูลไม่ครบถ้วนสำหรับ Item ID " + poItemId + " (กรุณาเลือกผล QC)";
                 logger.warn(errorMsg); errors.add(errorMsg);
                 allReceivedCompletely = false; // <<< ถือว่ารับไม่ครบถ้า Error
            } else if (quantityReceived == 0 && qcStatus != null && !qcStatus.isBlank()) {
                 // เลือกผล QC แต่ไม่กรอกจำนวน
                 String errorMsg = "ข้อมูลไม่ครบถ้วนสำหรับ Item ID " + poItemId + " (กรุณากรอกจำนวนที่รับ)";
                 logger.warn(errorMsg); errors.add(errorMsg);
                 allReceivedCompletely = false; // <<< ถือว่ารับไม่ครบถ้า Error
            }
            
            // (TODO: Logic ตรวจสอบ allReceivedCompletely ต้องซับซ้อนกว่านี้
            // โดยการนับจำนวนที่รับแล้ว (จาก QC records) เทียบกับจำนวนที่สั่ง (item.getQuantity())
            // ตอนนี้แค่เช็คว่ากรอกครบทุกช่องใน "รอบนี้" หรือไม่)

        } // จบ Loop for

        // 4. สรุปผลและอัปเดตสถานะ PO
        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดบางรายการ: " + String.join("; ", errors));
             logger.error("Intake process completed with errors for PO ID: {}", poId);
        } else if (itemsSuccessfullyProcessed == totalItemsInPo && somethingWasReceived) {
             // (Logic คำนวณสถานะใหม่)
             // (สมมติว่าถ้าไม่มี Error และรับของแล้ว -> ถือว่า Received)
            String finalPoStatus = allReceivedCompletely ? "Received" : "Partially Received"; // <<< ใช้ Flag ที่คำนวณ (ชั่วคราว)
            try {
                 purchaseOrderService.updatePurchaseOrderStatus(poId, finalPoStatus); //
                 redirectAttributes.addFlashAttribute("successMessage", "บันทึกการรับสินค้าสำหรับ PO '" + poId + "' สำเร็จ! สถานะ: " + finalPoStatus);
                 if (!successMessages.isEmpty()) {
                      redirectAttributes.addFlashAttribute("infoMessage", String.join("; ", successMessages));
                 }
                 logger.info("Intake process completed successfully for PO ID: {}", poId);
            } catch (RuntimeException e) {
                  String errorMsg = "เกิดข้อผิดพลาดในการอัปเดตสถานะ PO: " + e.getMessage();
                  logger.error(errorMsg, e);
                  redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            }
        } else if (!somethingWasReceived) {
             redirectAttributes.addFlashAttribute("infoMessage", "ยังไม่มีการรับสินค้าสำหรับ PO '" + poId + "' (ไม่ได้กรอกจำนวนรับ)");
             logger.info("Intake process submitted with no items received for PO ID: {}", poId);
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "ไม่สามารถประมวลผลการรับสินค้าได้ครบทุกรายการ");
             logger.error("Intake process did not complete fully for PO ID: {}", poId);
        }

        return "redirect:/purchase-orders";
    }
}