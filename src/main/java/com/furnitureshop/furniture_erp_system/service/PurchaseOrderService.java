package com.furnitureshop.furniture_erp_system.service;

import com.furnitureshop.furniture_erp_system.model.ProductVariant;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrder;
import com.furnitureshop.furniture_erp_system.model.PurchaseOrderItem;
import com.furnitureshop.furniture_erp_system.model.Supplier;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderItemRepository;
import com.furnitureshop.furniture_erp_system.repository.PurchaseOrderRepository;
import com.furnitureshop.furniture_erp_system.repository.SupplierRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // <<< Import BigDecimal
// ไม่ต้องใช้ RoundingMode ที่นี่ เว้นแต่จะคำนวณยอดรวมใน PO เอง
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseOrderService {

    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ProductVariantRepository variantRepository;

    @Transactional
    public PurchaseOrder createPurchaseOrder(String supplierId, List<PurchaseOrderItemDto> items) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));

        PurchaseOrder newPO = new PurchaseOrder();
        newPO.setPoID(generatePOId());
        newPO.setSupplier(supplier);
        newPO.setOrderDate(LocalDate.now());
        newPO.setStatus("Ordered");

        Set<PurchaseOrderItem> poItemsSet = new HashSet<>();

        for (PurchaseOrderItemDto itemDto : items) {
            ProductVariant variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("ProductVariant not found: " + itemDto.getVariantId()));

            PurchaseOrderItem poItem = new PurchaseOrderItem();
            poItem.setPurchaseOrder(newPO);
            poItem.setProductVariant(variant);
            poItem.setQuantity(itemDto.getQuantity());
            // *** ตั้งค่า Cost (เป็น BigDecimal) ***
            poItem.setCostPerUnit(itemDto.getCostPerUnit()); // <<< สมมติว่า DTO ส่ง BigDecimal มาให้แล้ว

            poItemsSet.add(poItem);
        }

        newPO.setPurchaseOrderItems(poItemsSet);
        return purchaseOrderRepository.save(newPO);
    }

    // Method สำหรับอัปเดตสถานะ PO (เช่น เป็น 'Received')
    @Transactional
    public PurchaseOrder updatePurchaseOrderStatus(String poId, String status) {
         PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new RuntimeException("PurchaseOrder not found: " + poId));
         po.setStatus(status);
         return purchaseOrderRepository.save(po);
    }


    private String generatePOId() {
        return "PO-" + System.currentTimeMillis();
    }

    // DTO อัปเดตให้ใช้ BigDecimal สำหรับ cost
    public static class PurchaseOrderItemDto {
        private String variantId;
        private int quantity;
        private BigDecimal costPerUnit; // <<< เปลี่ยนเป็น BigDecimal

        // Getters and Setters
        public String getVariantId() { return variantId; }
        public void setVariantId(String variantId) { this.variantId = variantId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getCostPerUnit() { return costPerUnit; } // <<< เปลี่ยนเป็น BigDecimal
        public void setCostPerUnit(BigDecimal costPerUnit) { this.costPerUnit = costPerUnit; } // <<< เปลี่ยนเป็น BigDecimal
    }
}