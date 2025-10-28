package com.furnitureshop.furniture_erp_system.controller; // เช็คชื่อแพ็กเกจให้ถูกต้อง

import org.springframework.stereotype.Controller; // บอกว่านี่คือ Controller
import org.springframework.web.bind.annotation.GetMapping; // จัดการคำขอแบบ GET

/**
 * Controller สำหรับจัดการคำขอทั่วไปเกี่ยวกับหน้าบ้าน/dashboard
 */
@Controller // บอก Spring Boot ว่าคลาสนี้จัดการคำขอจากเว็บ
public class HomeController {

    /**
     * จัดการคำขอที่เข้ามาที่ URL "/" (หน้าแรก) และแสดงหน้า dashboard
     * @GetMapping("/") คือการจับคู่คำขอ HTTP GET สำหรับหน้าแรก มาที่ method นี้
     * ค่าที่ return คือ "dashboard" ซึ่งจะบอก Spring MVC/Thymeleaf ให้ไปหาไฟล์ HTML
     * ชื่อ "dashboard.html" ในโฟลเดอร์ templates
     */
    @GetMapping("/") // เมื่อมีคนเข้าเว็บ "http://localhost:8080/"
    public String showDashboard() {
        // เดี๋ยวเราจะเพิ่ม Logic การส่งข้อมูล (เช่น Widgets) ไปให้ View ทีหลัง
        return "dashboard"; // อ้างอิงถึงไฟล์ "dashboard.html"
    }

    // เราสามารถเพิ่ม mapping สำหรับหน้าทั่วไปอื่นๆ ทีหลังได้ (เช่น "/login")
}