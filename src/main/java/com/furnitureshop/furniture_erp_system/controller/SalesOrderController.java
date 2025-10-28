package com.furnitureshop.furniture_erp_system.controller;

// --- Imports ---
import com.furnitureshop.furniture_erp_system.model.Customer;
import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.SalesOrder;
// DTO is nested in SalesOrderService, need static import or move DTO
import com.furnitureshop.furniture_erp_system.service.SalesOrderService.SalesOrderItemDto;
import com.furnitureshop.furniture_erp_system.repository.CustomerRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductRepository;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository; // Need this for listing SOs
import com.furnitureshop.furniture_erp_system.repository.UserRepository;
import com.furnitureshop.furniture_erp_system.service.SalesOrderService;

import com.fasterxml.jackson.databind.ObjectMapper; // <<< Import ตัวแปลง JSON

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sales-orders") // URL หลักสำหรับ SO
public class SalesOrderController {

    // --- Autowired Services and Repositories ---
    @Autowired private SalesOrderService salesOrderService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository; // To get the logged-in user later
    @Autowired private SalesOrderRepository salesOrderRepository; // For listing SOs

    /**
     * แสดงหน้ารายการ Sales Orders ทั้งหมด
     */
    @GetMapping("")
    public String listSalesOrders(Model model) {
        // ดึง SO ทั้งหมดจาก Repository
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        // ส่ง List ไปให้ View ชื่อ "salesOrderList"
        model.addAttribute("salesOrderList", salesOrderList);
        // แสดงผลด้วยไฟล์ sales-order-list.html
        return "sales-order-list";
    }

    /**
     * แสดงหน้าฟอร์มสำหรับสร้าง Sales Order ใหม่
     */
    @GetMapping("/new")
    public String showCreateSalesOrderForm(Model model) {
        // ดึงข้อมูลสำหรับ Dropdowns
        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();

        // สร้าง Object ว่างๆ (ถ้าจำเป็นสำหรับ data binding อื่นๆ)
        // SalesOrder salesOrder = new SalesOrder();
        // model.addAttribute("salesOrder", salesOrder); // อาจจะไม่จำเป็นสำหรับฟอร์มนี้

        // ส่งข้อมูล Dropdowns ไปให้ View
        model.addAttribute("customers", customers);
        model.addAttribute("products", products);

        // แสดงผลด้วยไฟล์ sales-order-form.html
        return "sales-order-form";
    }

    /**
     * จัดการการ submit ฟอร์มสร้าง Sales Order (รับ Items เป็น JSON String)
     * @param customerId รหัสลูกค้าที่เลือก
     * @param itemsJsonList รายการ JSON String ของ Items (จาก Hidden Inputs)
     * @param redirectAttributes ใช้ส่งข้อความแจ้งเตือนหลัง Redirect
     * @return Redirect ไปหน้ารายการ SO หรือกลับหน้าฟอร์มถ้า Error
     */
    @PostMapping("/save")
    public String createSalesOrder(
            @RequestParam("customerId") String customerId,
            @RequestParam(name = "itemsJson", required = false) List<String> itemsJsonList, // รับเป็น List<String>
            RedirectAttributes redirectAttributes) {

        List<SalesOrderItemDto> itemsDtoList = new ArrayList<>();

        // --- แปลง JSON String กลับเป็น List<SalesOrderItemDto> ---
        if (itemsJsonList != null && !itemsJsonList.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper(); // ตัวแปลง JSON
            for (String itemJson : itemsJsonList) {
                try {
                    // อ่าน JSON string แล้วแปลงเป็น DTO
                    SalesOrderItemDto dto = objectMapper.readValue(itemJson, SalesOrderItemDto.class);
                    // (Optional) เพิ่มการตรวจสอบค่าพื้นฐานใน DTO
                    if (dto.getVariantId() == null || dto.getVariantId().isEmpty() || dto.getQuantity() <= 0) {
                        throw new IllegalArgumentException("ข้อมูล Variant ID หรือ Quantity ไม่ถูกต้องใน JSON: " + itemJson);
                    }
                    itemsDtoList.add(dto);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "ข้อมูลรายการสินค้าไม่ถูกต้อง: " + e.getMessage());
                    return "redirect:/sales-orders/new"; // กลับหน้าฟอร์มพร้อม Error
                }
            }
        } else {
             // ถ้าไม่มี Item ส่งมาเลย ให้แจ้ง Error
             redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเพิ่มรายการสินค้าอย่างน้อย 1 รายการ");
             return "redirect:/sales-orders/new";
        }

        // --- ดึง User ID (ตัวอย่าง - ต้องเปลี่ยนเป็น Logic จริง) ---
        // ในระบบจริง ควรดึง User ID จาก Security Context (ผู้ใช้ที่ล็อกอิน)
        String userId = "U-002"; // <<<< Placeholder - ต้องเปลี่ยน!

        try {
            // --- เรียกใช้ Service เพื่อสร้าง SO และจองสต็อก ---
            SalesOrder createdOrder = salesOrderService.createSalesOrder(customerId, userId, itemsDtoList);
            // ส่งข้อความสำเร็จกลับไปแสดงผล
            redirectAttributes.addFlashAttribute("successMessage", "สร้าง Sales Order '" + createdOrder.getOrderID() + "' สำเร็จ!");
            // Redirect ไปหน้ารายการ SO
            return "redirect:/sales-orders";

        } catch (RuntimeException e) {
            // ถ้า Service โยน Error (เช่น สต็อกไม่พอ, ข้อมูลผิดพลาด)
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการสร้าง SO: " + e.getMessage());
            // Redirect กลับไปหน้าฟอร์มเดิม พร้อมแสดง Error
            return "redirect:/sales-orders/new";
        }
    }

    // --- (เพิ่ม Mapping สำหรับ Edit / Update / Delete / Payment ทีหลัง) ---

}