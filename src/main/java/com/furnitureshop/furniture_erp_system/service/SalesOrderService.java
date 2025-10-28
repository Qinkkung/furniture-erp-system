package com.furnitureshop.furniture_erp_system.service;

// --- Imports ---
import java.math.BigDecimal; // <<< Import BigDecimal
import java.math.RoundingMode; // <<< Import RoundingMode
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ... (Import Models และ Repositories เหมือนเดิม) ...
import com.furnitureshop.furniture_erp_system.model.Customer;
import com.furnitureshop.furniture_erp_system.model.Payment;
import com.furnitureshop.furniture_erp_system.model.ProductVariant;
import com.furnitureshop.furniture_erp_system.model.SalesOrder;
import com.furnitureshop.furniture_erp_system.model.SalesOrderItem;
import com.furnitureshop.furniture_erp_system.model.User;
import com.furnitureshop.furniture_erp_system.repository.CustomerRepository;
import com.furnitureshop.furniture_erp_system.repository.PaymentRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.SalesOrderItemRepository;
import com.furnitureshop.furniture_erp_system.repository.UserRepository;


@Service
public class SalesOrderService {

    // --- Autowired Repositories and Services (เหมือนเดิม) ---
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private SalesOrderItemRepository salesOrderItemRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryService inventoryService;
    @Autowired private PaymentRepository paymentRepository;

    // กำหนดค่าคงที่สำหรับการคำนวณ (ควรแยกไปไว้ไฟล์ Config)
    private static final BigDecimal VAT_RATE = new BigDecimal("0.07");
    private static final int SCALE = 2; // ทศนิยม 2 ตำแหน่ง
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP; // ปัดเศษแบบปกติ

    // --- Logic การสร้าง Sales Order (SO) ---
    @Transactional
    public SalesOrder createSalesOrder(String customerId, String userId, List<SalesOrderItemDto> items) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // ตรวจสอบสต็อก (ATS) ทั้งหมดก่อน (เหมือนเดิม)
        for (SalesOrderItemDto itemDto : items) {
            int ats = inventoryService.getAvailableToSell(itemDto.getVariantId());
            if (ats < itemDto.getQuantity()) {
                throw new RuntimeException("Stock not available for " + itemDto.getVariantId() + ". Required: " + itemDto.getQuantity() + ", Available: " + ats);
            }
        }

        // สร้าง SalesOrder (หัวบิล)
        SalesOrder newOrder = new SalesOrder();
        // ... (ตั้งค่า orderID, customer, user, orderDate, status เหมือนเดิม) ...
        newOrder.setOrderID(generateOrderId());
        newOrder.setCustomer(customer);
        newOrder.setUser(user);
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setStatus("Pending Deposit");

        Set<SalesOrderItem> orderItemsSet = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO; // <<< เริ่มต้น Subtotal เป็น BigDecimal 0

        // วนลูปสร้าง SalesOrderItem และจองสต็อก
        for (SalesOrderItemDto itemDto : items) {
            ProductVariant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("ProductVariant not found: " + itemDto.getVariantId()));

            SalesOrderItem orderItem = new SalesOrderItem();
            // ... (ตั้งค่า salesOrder, productVariant, quantity เหมือนเดิม) ...
            orderItem.setSalesOrder(newOrder);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(itemDto.getQuantity());

            // *** ตั้งค่า UnitPrice (เป็น BigDecimal) ***
            orderItem.setUnitPrice(variant.getUnitPrice()); // <<< ดึง BigDecimal จาก Variant

            orderItemsSet.add(orderItem);

            // *** คำนวณ Subtotal (ใช้ BigDecimal) ***
            BigDecimal itemTotal = variant.getUnitPrice() // ราคาต่อชิ้น (BigDecimal)
                                       .multiply(BigDecimal.valueOf(itemDto.getQuantity())); // คูณ จำนวน (แปลง int เป็น BigDecimal)
            subtotal = subtotal.add(itemTotal); // <<< ใช้ .add() สำหรับ BigDecimal

            // *** ทำการจองสต็อก (เหมือนเดิม) ***
            inventoryService.reserveStock(itemDto.getVariantId(), itemDto.getQuantity());
        }

        // *** คำนวณยอดรวม (ใช้ BigDecimal และ ปัดเศษ) ***
        BigDecimal vat = subtotal.multiply(VAT_RATE).setScale(SCALE, ROUNDING_MODE); // <<< คำนวณ VAT และปัดเศษ
        // ดึงค่าส่ง (สมมติว่า Customer มี Zone และ Zone มี Fee เป็น BigDecimal)
        BigDecimal deliveryFee = customer.getDeliveryZone() != null ? customer.getDeliveryZone().getDeliveryFee() : BigDecimal.ZERO;
        // (เพิ่ม Logic คำนวณ ServiceFee/Discount ตรงนี้ถ้าต้องการ)
        BigDecimal grandTotal = subtotal.add(vat).add(deliveryFee).setScale(SCALE, ROUNDING_MODE); // <<< บวก VAT, ค่าส่ง และปัดเศษ

        newOrder.setGrandTotal(grandTotal); // <<< เก็บ GrandTotal (BigDecimal)
        newOrder.setSalesOrderItems(orderItemsSet);

        // บันทึก SO (เหมือนเดิม)
        return salesOrderRepository.save(newOrder);
    }

    // --- Logic การบันทึกการชำระเงิน และ อัปเดตสถานะ SO ---
    @Transactional
    public Payment recordPaymentAndUpdateStatus(String orderId, BigDecimal amount, String paymentType) { // <<< รับ Amount เป็น BigDecimal
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found: " + orderId));

        // สร้าง Payment record
        Payment payment = new Payment();
        // ... (ตั้งค่า paymentID, salesOrder, paymentDate, paymentType เหมือนเดิม) ...
        payment.setPaymentID(generatePaymentId());
        payment.setSalesOrder(order);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentType(paymentType);
        payment.setAmount(amount); // <<< เก็บ Amount (BigDecimal)
        paymentRepository.save(payment);

        // คำนวณยอดชำระรวม (ใช้ BigDecimal Stream)
        List<Payment> payments = paymentRepository.findBySalesOrder_OrderID(orderId);
        BigDecimal totalPaid = payments.stream()
                                      .map(Payment::getAmount) // ดึงค่า Amount (BigDecimal)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add); // <<< รวมยอด BigDecimal

        // ตรวจสอบเงื่อนไขเพื่ออัปเดตสถานะ (ตัวอย่าง: มัดจำ 30%)
        BigDecimal depositRequired = order.getGrandTotal() // GrandTotal (BigDecimal)
                                        .multiply(new BigDecimal("0.30")) // คูณ 0.30 (BigDecimal)
                                        .setScale(SCALE, ROUNDING_MODE); // ปัดเศษ

        // เปรียบเทียบ BigDecimal ใช้ compareTo (0=เท่ากัน, >0=มากกว่า, <0=น้อยกว่า)
        if (totalPaid.compareTo(depositRequired) >= 0 && order.getStatus().equals("Pending Deposit")) {
            order.setStatus("Awaiting Shipment");
            salesOrderRepository.save(order);
        }

        return payment;
    }

    // --- Helper Methods (เหมือนเดิม) ---
    private String generateOrderId() { /*...*/ return "SO-" + System.currentTimeMillis(); }
    private String generatePaymentId() { /*...*/ return "P-" + System.currentTimeMillis(); }

    // --- DTO Class (เหมือนเดิม) ---
    public static class SalesOrderItemDto {
        private String variantId;
        private int quantity;
        // Getters/Setters
        public String getVariantId() { return variantId; }
        public void setVariantId(String variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}