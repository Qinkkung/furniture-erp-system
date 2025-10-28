package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // <<< ใช้ RestController สำหรับ API

import java.util.Map; // <<< Import Map

@RestController // <<< ใช้ @RestController สำหรับ API ที่ส่ง JSON กลับอย่างเดียว
@RequestMapping("/api/inventory") // URL หลักสำหรับ Inventory API
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * API Endpoint สำหรับดึง ATS (Available-to-Sell) ของ Variant ที่กำหนด
     * @param variantId รหัส SKU
     * @return JSON object ที่มี key "ats" และ value เป็นจำนวนสต็อกที่ขายได้
     */
    @GetMapping("/ats/{variantId}")
    public ResponseEntity<Map<String, Integer>> getAvailableToSell(@PathVariable String variantId) {
        try {
            int ats = inventoryService.getAvailableToSell(variantId);
            // สร้าง JSON object แบบง่ายๆ {"ats": 8}
            return ResponseEntity.ok(Map.of("ats", ats));
        } catch (RuntimeException e) {
            // ถ้าหา Variant ไม่เจอ หรือเกิด Error อื่น
            return ResponseEntity.notFound().build(); // ส่ง 404 Not Found
        }
    }
}