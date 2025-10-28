package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.Customer;
import com.furnitureshop.furniture_erp_system.model.DeliveryZone;
import com.furnitureshop.furniture_erp_system.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // ใช้ RestController ด้วย

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController // ใช้ RestController สำหรับ API
@RequestMapping("/api/customers") // URL หลักสำหรับ Customer API
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * API Endpoint สำหรับดึงข้อมูลลูกค้าและค่าส่ง
     * @param customerId รหัสลูกค้า
     * @return JSON object ที่มี key "deliveryFee" และอาจมี key อื่นๆ
     */
    @GetMapping("/info/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerInfo(@PathVariable String customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            DeliveryZone zone = customer.getDeliveryZone();
            BigDecimal fee = (zone != null) ? zone.getDeliveryFee() : BigDecimal.ZERO;

            // สร้าง JSON object {"deliveryFee": 300.00, "zoneName": "กทม. ชั้นใน"}
            Map<String, Object> response = Map.of(
                    "deliveryFee", fee,
                    "zoneName", (zone != null ? zone.getZoneName() : "N/A")
                    // สามารถเพิ่มข้อมูลลูกค้าอื่นๆ ที่ต้องการแสดงได้
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- สามารถเพิ่ม Controller (@Controller) สำหรับหน้าจัดการลูกค้า (CRUD) ที่นี่ได้ ---
    // @Controller
    // @RequestMapping("/customers") // แยก URL สำหรับหน้าเว็บ
    // public class CustomerWebController { ... }
}