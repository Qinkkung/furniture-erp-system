package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.dto.ProductVariantDropdownDto;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.Supplier;
import com.furnitureshop.furniture_erp_system.model.ProductVariant;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.SupplierRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;
import com.furnitureshop.furniture_erp_system.service.ProductService;
import com.furnitureshop.furniture_erp_system.service.PurchaseOrderService;
import com.furnitureshop.furniture_erp_system.service.PurchaseOrderService.PurchaseOrderItemDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;


@Controller
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {

    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseOrderService purchaseOrderService;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private ProductService productService;

    /**
     * แสดงรายการ PO ทั้งหมด (แก้ไขให้เรียก Method ใหม่)
     */
    @GetMapping("")
    public String listPurchaseOrders(Model model) {
        // *** แก้ไข: เรียก findAllWithSupplier() แทน findAll() ***
        List<PurchaseOrder> poList = purchaseOrderRepository.findAllWithSupplier();
        model.addAttribute("poList", poList);
        return "purchase-order-list";
    }

    /**
     * แสดงฟอร์มสร้าง PO ใหม่ (ใช้ DTO สำหรับ Variants)
     */
    @GetMapping("/new")
    public String showCreatePurchaseOrderForm(Model model) {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<ProductVariantDropdownDto> variantsDto = productService.getAllVariantsForDropdown();
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("variantsDto", variantsDto);
        return "purchase-order-form";
    }

    /**
     * จัดการการ submit ฟอร์มสร้าง PO (แก้ไขให้ Decode JSON)
     */
    @PostMapping("/save")
    public String createPurchaseOrder(
            @RequestParam("supplierId") String supplierId,
            @RequestParam(name = "itemsJson", required = false) List<String> encodedItemsJsonList,
            RedirectAttributes redirectAttributes) {

        List<PurchaseOrderItemDto> itemsDtoList = new ArrayList<>();
        if (encodedItemsJsonList != null && !encodedItemsJsonList.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (String encodedItemJson : encodedItemsJsonList) {
                try {
                    String itemJson = URLDecoder.decode(encodedItemJson, StandardCharsets.UTF_8.toString());
                    PurchaseOrderItemDto dto = objectMapper.readValue(itemJson, PurchaseOrderItemDto.class);
                    if (dto.getVariantId() == null || dto.getVariantId().isEmpty()
                        || dto.getQuantity() <= 0
                        || dto.getCostPerUnit() == null || dto.getCostPerUnit().compareTo(BigDecimal.ZERO) < 0) {
                           throw new IllegalArgumentException("ข้อมูล SKU, จำนวน, หรือต้นทุนไม่ถูกต้อง");
                    }
                    itemsDtoList.add(dto);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "ข้อมูลรายการสินค้าไม่ถูกต้อง (Decode/Parse Error): " + e.getMessage());
                    return "redirect:/purchase-orders/new";
                }
            }
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเพิ่มรายการสินค้าอย่างน้อย 1 รายการ");
             return "redirect:/purchase-orders/new";
        }

        try {
            PurchaseOrder createdPO = purchaseOrderService.createPurchaseOrder(supplierId, itemsDtoList);
            redirectAttributes.addFlashAttribute("successMessage", "สร้าง Purchase Order '" + createdPO.getPoID() + "' สำเร็จ!");
            return "redirect:/purchase-orders";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการสร้าง PO: " + e.getMessage());
            return "redirect:/purchase-orders/new";
        }
    }

    // --- (TODO: Add /view/{id} mapping later) ---

}