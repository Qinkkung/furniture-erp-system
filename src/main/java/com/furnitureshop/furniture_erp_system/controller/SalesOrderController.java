package com.furnitureshop.furniture_erp_system.controller;

// --- Imports ที่จำเป็นทั้งหมด ---
import com.furnitureshop.furniture_erp_system.model.Customer;
import com.furnitureshop.furniture_erp_system.model.Payment;
import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.service.SalesOrderService.SalesOrderItemDto;
import com.furnitureshop.furniture_erp_system.repository.CustomerRepository;
import com.furnitureshop.furniture_erp_system.repository.PaymentRepository; // <<< ต้องมีบรรทัดนี้
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/sales-orders") 
public class SalesOrderController {

    // --- Autowired Services and Repositories ---
    @Autowired private SalesOrderService salesOrderService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private PaymentRepository paymentRepository; // <<< Fix: Autowired ถูกต้องแล้ว

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
            @RequestParam(name = "itemsJson", required = false) List<String> encodedItemsJsonList,
            RedirectAttributes redirectAttributes) {

        List<SalesOrderItemDto> itemsDtoList = new ArrayList<>();
        if (encodedItemsJsonList != null && !encodedItemsJsonList.isEmpty()) { 
            ObjectMapper objectMapper = new ObjectMapper();
            for (String encodedItemJson : encodedItemsJsonList) { 
                try {
                    String itemJson = URLDecoder.decode(encodedItemJson, StandardCharsets.UTF_8.toString());

                    SalesOrderItemDto dto = objectMapper.readValue(itemJson, SalesOrderItemDto.class); 
                    
                    if (dto.getVariantId() == null || dto.getVariantId().isEmpty() || dto.getQuantity() <= 0) {
                        throw new IllegalArgumentException("ข้อมูล Variant ID หรือ Quantity ไม่ถูกต้อง");
                    }
                    itemsDtoList.add(dto);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "ข้อมูลรายการสินค้าไม่ถูกต้อง (Decode/Parse Error): " + e.getMessage());
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
    public String showPaymentForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) { 
        Optional<SalesOrder> orderOpt = salesOrderRepository.findById(id); 
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
        
        Optional<SalesOrder> orderOpt = salesOrderRepository.findByIdWithDetails(id);

        if (orderOpt.isPresent()) {
            SalesOrder order = orderOpt.get();

            // Fix: ดึงข้อมูลล่าสุดจาก Repository โดยตรง
            List<Payment> existingPayments = paymentRepository.findBySalesOrder_OrderID(id);
            
            BigDecimal totalPaid = BigDecimal.ZERO; 
            if (existingPayments != null) { 
                totalPaid = existingPayments.stream()
                               .map(Payment::getAmount)
                               .filter(java.util.Objects::nonNull)
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            model.addAttribute("order", order);
            model.addAttribute("totalPaidForView", totalPaid); 

            return "sales-order-view"; 
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Sales Order ID: " + id);
            return "redirect:/sales-orders";
        }
    }

    // --- (เพิ่ม Mapping สำหรับ Edit / Update / Delete SO ทีหลัง) ---

}