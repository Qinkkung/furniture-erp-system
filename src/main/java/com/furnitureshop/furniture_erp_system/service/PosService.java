package com.furnitureshop.furniture_erp_system.service;

import com.furnitureshop.furniture_erp_system.model.*;
import com.furnitureshop.furniture_erp_system.repository.PosTransactionRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PosService {

    // --- Autowired Repositories and Services ---
    @Autowired private PosTransactionRepository posTransactionRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryService inventoryService; // (ตัวตัดสต็อก)

    // (ค่าคงที่สำหรับคำนวณ VAT)
    private static final BigDecimal VAT_RATE = new BigDecimal("0.07");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Logic หลักสำหรับประมวลผลการขายหน้าร้าน (POS)
     */
    @Transactional
    public PosTransaction createPosSale(List<PosItemDto> items, User user) {

        // 1. (ตรวจสอบสต็อก) วนลูปตรวจสอบ ATS ของ "ทุก" รายการก่อน
        for (PosItemDto itemDto : items) {
            int ats = inventoryService.getAvailableToSell(itemDto.getVariantId());
            if (ats < itemDto.getQuantity()) {
                // ถ้าสต็อกไม่พอ ให้ยกเลิกทันที
                throw new RuntimeException("Stock not available for " + itemDto.getVariantId() + ". ATS: " + ats);
            }
        }

        // 2. (สร้างหัวบิล) ถ้าสต็อกพอ ให้เริ่มสร้าง PosTransaction
        PosTransaction transaction = new PosTransaction();
        transaction.setPosTransID(generatePosId());
        transaction.setUser(user);
        transaction.setTransactionDate(LocalDateTime.now());

        Set<PosTransactionItem> transactionItems = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // 3. (สร้างรายการย่อย) วนลูปอีกครั้งเพื่อสร้างรายการ และ "ตัดสต็อก"
        for (PosItemDto itemDto : items) {
            ProductVariant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + itemDto.getVariantId()));

            // สร้างรายการในบิล
            PosTransactionItem item = new PosTransactionItem();
            item.setPosTransaction(transaction);
            item.setProductVariant(variant);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(variant.getUnitPrice()); // (ดึงราคาขายจาก Variant)
            transactionItems.add(item);

            // คำนวณยอดรวม
            subtotal = subtotal.add(variant.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            
            // 4. (*** LOGIC สำคัญ ***)
            // เรียกใช้ deductStockPOS เพื่อ "ตัดสต็อก OnHand ทันที"
            // ตาม Logic 3.3.2 [cite: 269] และ 3.9 [cite: 387]
            inventoryService.deductStockPOS(itemDto.getVariantId(), itemDto.getQuantity());
        }

        // 5. (คำนวณยอดสุทธิ)
        BigDecimal vat = subtotal.multiply(VAT_RATE).setScale(SCALE, ROUNDING_MODE);
        BigDecimal grandTotal = subtotal.add(vat).setScale(SCALE, ROUNDING_MODE);
        
        transaction.setGrandTotal(grandTotal);
        transaction.setPosTransactionItems(transactionItems);

        // 6. (บันทึก) บันทึกหัวบิล (และรายการย่อยจะถูกบันทึกตามไปด้วย)
        return posTransactionRepository.save(transaction);
    }
    
    // (Helper สร้าง ID)
    private String generatePosId() { 
        return "POS-" + System.currentTimeMillis(); 
    }

    // --- DTO สำหรับรับข้อมูลจากฟอร์ม ---
    // (เราสร้าง DTO ไว้ในคลาส Service เลย เพื่อง่ายต่อการจัดการ)
    public static class PosItemDto {
        private String variantId;
        private int quantity;
        
        // (ต้องมี Getters/Setters ให้ Jackson ใช้งาน)
        public String getVariantId() { return variantId; }
        public void setVariantId(String variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}