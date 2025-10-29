package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.dto.ProductVariantDropdownDto; // <<< Import DTO
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.Supplier;
import com.furnitureshop.furniture_erp_system.model.ProductVariant; // <<< Import ProductVariant
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.SupplierRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository; // <<< Import Variant Repo
import com.furnitureshop.furniture_erp_system.service.ProductService; // <<< Import ProductService
import com.furnitureshop.furniture_erp_system.service.PurchaseOrderService;
// Import DTO ที่ซ้อนอยู่ใน Service (หรือย้าย DTO ออกมาไฟล์แยก)
import com.furnitureshop.furniture_erp_system.service.PurchaseOrderService.PurchaseOrderItemDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // ใช้ * เพื่อ import ทั้งหมด
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal; // <<< Import BigDecimal
import java.net.URLDecoder; // <<< Import URLDecoder
import java.nio.charset.StandardCharsets; // <<< Import StandardCharsets
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper; // <<< Import ObjectMapper


@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseOrderService purchaseOrderService;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ProductVariantRepository variantRepository; // <<< เพิ่ม Autowired Variant Repo (อาจไม่จำเป็นถ้าเรียกผ่าน Service)
    @Autowired private ProductService productService; // <<< เพิ่ม Autowired ProductService

    /**
     * แสดงรายการ PO ทั้งหมด
     */
    @GetMapping("")
    public String listPurchaseOrders(Model model) {
        List<PurchaseOrder> poList = purchaseOrderRepository.findAll(); // Add sorting later if needed
        model.addAttribute("poList", poList);
        return "purchase-order-list";
    }

    /**
     * แสดงฟอร์มสร้าง PO ใหม่ (ใช้ DTO สำหรับ Variants)
     */
    @GetMapping("/new")
    public String showCreatePurchaseOrderForm(Model model) {
        List<Supplier> suppliers = supplierRepository.findAll();
        // *** เรียกใช้ Service เพื่อดึง DTO ***
        List<ProductVariantDropdownDto> variantsDto = productService.getAllVariantsForDropdown(); // <<< ใช้ DTO

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("variantsDto", variantsDto); // <<< ส่ง DTO ไปชื่อ "variantsDto"

        return "purchase-order-form";
    }

    /**
     * จัดการการ submit ฟอร์มสร้าง PO (แก้ไขให้ Decode JSON)
     */
    @PostMapping("/save")
    public String createPurchaseOrder(
            @RequestParam("supplierId") String supplierId,
            @RequestParam(name = "itemsJson", required = false) List<String> encodedItemsJsonList, // <<< รับค่าที่ Encode มา
            RedirectAttributes redirectAttributes) {

        List<PurchaseOrderItemDto> itemsDtoList = new ArrayList<>();
        if (encodedItemsJsonList != null && !encodedItemsJsonList.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (String encodedItemJson : encodedItemsJsonList) { // <<< วนลูปค่าที่ Encode มา
                try {
                    // *** Decode the JSON string first ***
                    String itemJson = URLDecoder.decode(encodedItemJson, StandardCharsets.UTF_8.toString()); // <<< Decode กลับ
                    // Convert JSON string back to DTO
                    PurchaseOrderItemDto dto = objectMapper.readValue(itemJson, PurchaseOrderItemDto.class);
                    // Basic validation
                    if (dto.getVariantId() == null || dto.getVariantId().isEmpty()
                        || dto.getQuantity() <= 0
                        || dto.getCostPerUnit() == null || dto.getCostPerUnit().compareTo(BigDecimal.ZERO) < 0) {
                           throw new IllegalArgumentException("ข้อมูล SKU, จำนวน, หรือต้นทุนไม่ถูกต้อง");
                    }
                    itemsDtoList.add(dto);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "ข้อมูลรายการสินค้าไม่ถูกต้อง (Decode/Parse Error): " + e.getMessage());
                    return "redirect:/purchase-orders/new"; // Go back to form with error
                }
            }
        } else {
             // If no items were added
             redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเพิ่มรายการสินค้าอย่างน้อย 1 รายการ");
             return "redirect:/purchase-orders/new";
        }

        try {
            // Call the service to create the PO
            PurchaseOrder createdPO = purchaseOrderService.createPurchaseOrder(supplierId, itemsDtoList);
            redirectAttributes.addFlashAttribute("successMessage", "สร้าง Purchase Order '" + createdPO.getPoID() + "' สำเร็จ!");
            return "redirect:/purchase-orders"; // Redirect to the PO list page

        } catch (RuntimeException e) {
            // If service throws an error (e.g., supplier not found)
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการสร้าง PO: " + e.getMessage());
            return "redirect:/purchase-orders/new"; // Go back to form with error
        }
    }

    // --- (TODO: Add /view/{id} mapping later) ---

}