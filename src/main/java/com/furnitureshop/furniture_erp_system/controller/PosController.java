package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.dto.ProductVariantDropdownDto;
import com.furnitureshop.furniture_erp_system.model.User;
import com.furnitureshop.furniture_erp_system.repository.UserRepository;
import com.furnitureshop.furniture_erp_system.service.ProductService;
import com.furnitureshop.furniture_erp_system.service.PosService;
import com.furnitureshop.furniture_erp_system.service.PosService.PosItemDto; // (Import DTO ที่เราสร้างไว้)

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pos") // URL หลักสำหรับ POS
public class PosController {

    @Autowired private ProductService productService;
    @Autowired private PosService posService;
    @Autowired private UserRepository userRepository; // (สำหรับดึง User ที่ Login อยู่)

    /**
     * แสดงหน้าขาย POS
     * (เราต้องดึงข้อมูล SKU ทั้งหมดมาให้ JavaScript เลือก)
     */
    @GetMapping("")
    public String showPosPage(Model model) {
        // (ใช้ Method เดียวกับที่ PO Form ใช้ เพื่อดึง DTO ของ Variant ทั้งหมด)
        List<ProductVariantDropdownDto> variantsDto = productService.getAllVariantsForDropdown();
        model.addAttribute("variantsDto", variantsDto);
        return "pos-form"; // (เราจะสร้างไฟล์ pos-form.html ต่อไป)
    }

    /**
     * API Endpoint สำหรับรับข้อมูลการขาย
     * (JavaScript จะยิงข้อมูลมาที่นี่)
     */
    @PostMapping("/save")
    @ResponseBody // (บอกว่าให้ส่งคำตอบกลับเป็น JSON ไม่ใช่ HTML)
    public ResponseEntity<?> processPosSale(
            @RequestBody List<PosItemDto> items, // (รับ List ของ DTO จาก JavaScript)
            @AuthenticationPrincipal User userDetails, // (ดึง User ที่ Login อยู่)
            RedirectAttributes redirectAttributes) {
        
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "ไม่มีรายการสินค้า"));
        }

        try {
            // (ดึง Entity User จริงๆ จาก ID)
            User currentUser = userRepository.findById(userDetails.getUserID())
                .orElseThrow(() -> new RuntimeException("ไม่พบ User"));

            // (เรียก Service ที่เราสร้างไว้)
            posService.createPosSale(items, currentUser);
            
            // (ถ้าสำเร็จ ส่ง JSON กลับไปบอก JavaScript)
            return ResponseEntity.ok(java.util.Map.of("message", "บันทึกการขายสำเร็จ!"));

        } catch (RuntimeException e) {
            // (ถ้า Error เช่น สต็อกไม่พอ ส่ง JSON กลับไปบอก JavaScript)
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "เกิดข้อผิดพลาด: " + e.getMessage()));
        }
    }
}