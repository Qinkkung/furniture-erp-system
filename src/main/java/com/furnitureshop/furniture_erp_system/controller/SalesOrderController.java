package com.furnitureshop.furniture_erp_system.controller;

// --- Imports ---
import com.furnitureshop.furniture_erp_system.model.Customer;
import com.furnitureshop.furniture_erp_system.model.Payment;
import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.SalesOrder;
// DTO is nested in SalesOrderService, need static import or move DTO
import com.furnitureshop.furniture_erp_system.service.SalesOrderService.SalesOrderItemDto;
import com.furnitureshop.furniture_erp_system.repository.CustomerRepository;
import com.furnitureshop.furniture_erp_system.repository.PaymentRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductRepository;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.UserRepository;
import com.furnitureshop.furniture_erp_system.service.SalesOrderService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Needed if using stream().map() etc.

@Controller
@RequestMapping("/sales-orders") // URL หลักสำหรับ SO
public class SalesOrderController {

    // --- Autowired Services and Repositories ---
    @Autowired private SalesOrderService salesOrderService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private PaymentRepository paymentRepository;

    /**
     * แสดงหน้ารายการ Sales Orders ทั้งหมด
     */
    @GetMapping("")
    public String listSalesOrders(Model model) {
        List<SalesOrder> salesOrderList = salesOrderRepository.findAll();
        model.addAttribute("salesOrderList", salesOrderList);
        return "sales-order-list";
    }

    /**
     * แสดงหน้าฟอร์มสำหรับสร้าง Sales Order ใหม่
     */
    @GetMapping("/new")
    public String showCreateSalesOrderForm(Model model) {
        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("products", products);
        return "sales-order-form";
    }

    /**
     * จัดการการ submit ฟอร์มสร้าง Sales Order
     */
    @PostMapping("/save")
    public String createSalesOrder(
            @RequestParam("customerId") String customerId,
            @RequestParam(name = "itemsJson", required = false) List<String> itemsJsonList,
            RedirectAttributes redirectAttributes) {

        List<SalesOrderItemDto> itemsDtoList = new ArrayList<>();
        if (itemsJsonList != null && !itemsJsonList.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            for (String itemJson : itemsJsonList) {
                try {
                    SalesOrderItemDto dto = objectMapper.readValue(itemJson, SalesOrderItemDto.class);
                    if (dto.getVariantId() == null || dto.getVariantId().isEmpty() || dto.getQuantity() <= 0) {
                        throw new IllegalArgumentException("ข้อมูล Variant ID หรือ Quantity ไม่ถูกต้อง");
                    }
                    itemsDtoList.add(dto);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "ข้อมูลรายการสินค้าไม่ถูกต้อง: " + e.getMessage());
                    return "redirect:/sales-orders/new";
                }
            }
        } else {
             redirectAttributes.addFlashAttribute("errorMessage", "กรุณาเพิ่มรายการสินค้าอย่างน้อย 1 รายการ");
             return "redirect:/sales-orders/new";
        }

        String userId = "U-002"; // <<< Placeholder - ต้องเปลี่ยนเป็น Logic ดึง User ที่ Login จริง

        try {
            SalesOrder createdOrder = salesOrderService.createSalesOrder(customerId, userId, itemsDtoList);
            redirectAttributes.addFlashAttribute("successMessage", "สร้าง Sales Order '" + createdOrder.getOrderID() + "' สำเร็จ!");
            return "redirect:/sales-orders";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการสร้าง SO: " + e.getMessage());
            return "redirect:/sales-orders/new";
        }
    }

    // --- Methods สำหรับ Payment ---

    /**
     * แสดงหน้าฟอร์มสำหรับบันทึกการชำระเงินของ SO ที่กำหนด
     */
    @GetMapping("/payment/{id}")
    public String showPaymentForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) { // Added RedirectAttributes
        Optional<SalesOrder> orderOpt = salesOrderRepository.findById(id); // Use findById first
        if (!orderOpt.isPresent()) {
             redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Sales Order ID: " + id);
             return "redirect:/sales-orders";
        }
        SalesOrder order = orderOpt.get();

        Payment payment = new Payment();
        payment.setSalesOrder(order);

        List<Payment> existingPayments = paymentRepository.findBySalesOrder_OrderID(id);
        BigDecimal totalPaid = existingPayments.stream()
                                      .map(Payment::getAmount)
                                      .filter(java.util.Objects::nonNull)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = order.getGrandTotal() != null ? order.getGrandTotal().subtract(totalPaid) : BigDecimal.ZERO;
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
             remainingAmount = BigDecimal.ZERO;
        }

        model.addAttribute("payment", payment);
        model.addAttribute("order", order);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("remainingAmount", remainingAmount);

        return "payment-form";
    }

    /**
     * จัดการการ submit ฟอร์มบันทึกการชำระเงิน
     */
    @PostMapping("/payment/save/{id}")
    public String savePayment(@PathVariable("id") String id,
                              @RequestParam("amount") BigDecimal amount,
                              @RequestParam("paymentType") String paymentType,
                              RedirectAttributes redirectAttributes) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
             redirectAttributes.addFlashAttribute("errorMessage", "จำนวนเงินต้องมากกว่า 0");
             return "redirect:/sales-orders/payment/" + id;
        }

        try {
            salesOrderService.recordPaymentAndUpdateStatus(id, amount, paymentType);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึกการชำระเงินสำหรับ Order '" + id + "' สำเร็จ!");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการบันทึก: " + e.getMessage());
            return "redirect:/sales-orders/payment/" + id;
        }

        return "redirect:/sales-orders";
    }

    /**
     * แสดงหน้ารายละเอียดของ Sales Order ที่กำหนด
     */
    @GetMapping("/view/{id}")
    public String viewSalesOrder(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        // *** ใช้ Method ใหม่ที่ Join Fetch ข้อมูลมาครบ ***
        Optional<SalesOrder> orderOpt = salesOrderRepository.findByIdWithDetails(id);

        if (orderOpt.isPresent()) {
            SalesOrder order = orderOpt.get();

            // *** คำนวณ totalPaid ตรงนี้เลย ***
            BigDecimal totalPaid = BigDecimal.ZERO; // เริ่มต้นเป็น 0
            // ตรวจสอบ null ก่อนเข้าถึง payments
            if (order.getPayments() != null) {
                totalPaid = order.getPayments().stream()
                               .map(Payment::getAmount) // ดึง BigDecimal Amount
                               .filter(java.util.Objects::nonNull) // กรองค่า null
                               .reduce(BigDecimal.ZERO, BigDecimal::add); // รวมยอด
            }

            // ส่งข้อมูล SO และ totalPaid ไปให้ View
            model.addAttribute("order", order);
            model.addAttribute("totalPaidForView", totalPaid); // <<< ส่ง totalPaid ไปในชื่อใหม่

            return "sales-order-view"; // <<< ไฟล์ HTML ที่เราจะแก้ไข Thymeleaf
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Sales Order ID: " + id);
            return "redirect:/sales-orders";
        }
    }

    // --- (เพิ่ม Mapping สำหรับ Edit / Update / Delete SO ทีหลัง) ---

}