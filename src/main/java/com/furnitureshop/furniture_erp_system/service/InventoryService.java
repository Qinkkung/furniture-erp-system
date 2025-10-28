package com.furnitureshop.furniture_erp_system.service;

import org.springframework.beans.factory.annotation.Autowired; // เครื่องมือสำหรับ "ฉีด" Repository เข้ามา
import org.springframework.stereotype.Service; // บอก Spring Boot ว่านี่คือ Service
import org.springframework.transaction.annotation.Transactional; // สำหรับจัดการ Transaction (สำคัญมาก)

import com.furnitureshop.furniture_erp_system.model.Inventory;
import com.furnitureshop.furniture_erp_system.repository.InventoryRepository; // Import Repository ที่เราสร้าง
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;

import java.util.Optional; // สำหรับจัดการกรณีหาข้อมูลไม่เจอ

@Service // บอก Spring Boot ว่านี่คือ Service Class
public class InventoryService {

    // --- การ "ฉีด" Dependencies ---
    // @Autowired คือ "เวทมนตร์" ของ Spring Boot ที่จะเอา Repository มาใส่ให้เราอัตโนมัติ
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductVariantRepository variantRepository; // เราอาจต้องใช้ variantRepo ด้วย

    // --- Logic การคำนวณ ATS (จาก Pseudocode) ---
    /**
     * คำนวณสต็อกที่ขายได้จริง (ATS) สำหรับ SKU ที่กำหนด
     * @param variantId รหัส SKU
     * @return จำนวน ATS หรือ 0 ถ้าไม่พบข้อมูล
     */
    public int getAvailableToSell(String variantId) {
        // ใช้ Repository ค้นหาแถว Inventory ของ SKU นี้
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductVariant_VariantID(variantId); // เราต้องสร้าง method นี้ใน InventoryRepository

        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            // เรียกใช้ method ที่เราเขียนไว้ใน Inventory.java
            return inventory.getAvailableToSell();
        } else {
            // ถ้าไม่เจอแถว Inventory ของ SKU นี้เลย (อาจจะยังไม่เคยรับเข้า) ให้คืนค่า 0
            return 0;
        }
    }

    // --- Logic การจองสต็อก (จาก Pseudocode) ---
    /**
     * ทำการจองสต็อก (เพิ่ม QuantityReserved)
     * @param variantId รหัส SKU ที่จะจอง
     * @param quantity จำนวนที่ต้องการจอง
     * @throws RuntimeException ถ้าสต็อก ATS ไม่พอ หรือ ไม่พบข้อมูล
     */
    @Transactional // สำคัญมาก! ทำให้การทำงานทั้งหมดนี้ ถ้าผิดพลาดจะ Rollback กลับไปเหมือนเดิม
    public void reserveStock(String variantId, int quantity) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductVariant_VariantID(variantId);

        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();

            // ตรวจสอบ ATS ก่อนจอง
            if (inventory.getAvailableToSell() < quantity) {
                throw new RuntimeException("Stock not available for reservation: " + variantId);
            }

            // ทำการจอง (เพิ่ม Reserved)
            inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
            inventoryRepository.save(inventory); // บันทึกการเปลี่ยนแปลงลงฐานข้อมูล
        } else {
            throw new RuntimeException("Inventory record not found for variant: " + variantId);
        }
    }

    // --- Logic การตัดสต็อกจริง (จาก Pseudocode - สำหรับ SO Delivered) ---
    /**
     * ทำการตัดสต็อกจริง (ลด OnHand และ Reserved) หลังจากส่งของสำเร็จ
     * @param variantId รหัส SKU ที่จะตัด
     * @param quantity จำนวนที่ส่ง
     * @throws RuntimeException ถ้าไม่พบข้อมูล
     */
    @Transactional
    public void deductStock(String variantId, int quantity) {
         Optional<Inventory> inventoryOpt = inventoryRepository.findByProductVariant_VariantID(variantId);

        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();

            // ลด OnHand
            int newOnHand = inventory.getQuantityOnHand() - quantity;
            if (newOnHand < 0) { // ป้องกันสต็อกติดลบ (ควรเช็คก่อนหน้านี้แล้ว แต่เช็คอีกรอบเพื่อความปลอดภัย)
               newOnHand = 0; // หรือจะโยน Error ก็ได้
            }
             inventory.setQuantityOnHand(newOnHand);

            // ลด Reserved
            int newReserved = inventory.getQuantityReserved() - quantity;
             if (newReserved < 0) {
                newReserved = 0; // ยอดจองไม่ควรติดลบ
             }
            inventory.setQuantityReserved(newReserved);

            inventoryRepository.save(inventory);
        } else {
             throw new RuntimeException("Inventory record not found for variant: " + variantId);
        }
    }

     // --- Logic การตัดสต็อกทันที (จาก Pseudocode - สำหรับ POS Paid) ---
     /**
      * ทำการตัดสต็อกทันที (ลด OnHand อย่างเดียว) สำหรับการขาย POS
      * @param variantId รหัส SKU ที่จะตัด
      * @param quantity จำนวนที่ขาย
      * @throws RuntimeException ถ้าสต็อก ATS ไม่พอ หรือ ไม่พบข้อมูล
      */
     @Transactional
     public void deductStockPOS(String variantId, int quantity) {
         Optional<Inventory> inventoryOpt = inventoryRepository.findByProductVariant_VariantID(variantId);

         if (inventoryOpt.isPresent()) {
             Inventory inventory = inventoryOpt.get();

             // ตรวจสอบ ATS ก่อนตัด (สำคัญ!)
             if (inventory.getAvailableToSell() < quantity) {
                 throw new RuntimeException("Stock not available for POS sale: " + variantId);
             }

             // ลด OnHand ทันที
             inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantity);
             inventoryRepository.save(inventory);
         } else {
             throw new RuntimeException("Inventory record not found for variant: " + variantId);
         }
     }

     // --- Logic การเพิ่มสต็อก (จาก Pseudocode - สำหรับ QC Pass) ---
     /**
      * เพิ่มสต็อกในมือ (OnHand) หลังจาก QC ผ่าน
      * @param variantId รหัส SKU ที่จะเพิ่ม
      * @param quantity จำนวนที่รับเข้า
      */
     @Transactional
     public void addStockOnHand(String variantId, int quantity) {
         Optional<Inventory> inventoryOpt = inventoryRepository.findByProductVariant_VariantID(variantId);
         Inventory inventory;

         if (inventoryOpt.isPresent()) {
             // ถ้ามีแถว Inventory ของ SKU นี้อยู่แล้ว ให้อัปเดต OnHand
             inventory = inventoryOpt.get();
             inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantity);
         } else {
             // ถ้ายังไม่มีแถว Inventory (เช่น เป็น SKU ใหม่ที่เพิ่งรับเข้าครั้งแรก)
             // ให้สร้างแถวใหม่
             inventory = new Inventory();
             // ต้องไปดึง ProductVariant มาใส่ก่อน (สำคัญ!)
             Optional<com.furnitureshop.furniture_erp_system.model.ProductVariant> variantOpt = variantRepository.findById(variantId);
             if (!variantOpt.isPresent()) {
                 throw new RuntimeException("ProductVariant not found: " + variantId);
             }
             inventory.setProductVariant(variantOpt.get());
             inventory.setQuantityOnHand(quantity);
             inventory.setQuantityReserved(0); // เริ่มต้นที่จอง 0
         }
         inventoryRepository.save(inventory); // บันทึก (ทั้งอัปเดตและสร้างใหม่)
     }
}