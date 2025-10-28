package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.ProductVariant; // <<< Import ProductVariant
import com.furnitureshop.furniture_erp_system.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // <<< Import ResponseEntity
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody; // <<< Import ResponseBody

import java.util.Collections; // <<< Import Collections
import java.util.List;
import java.util.Optional; // <<< Import Optional
import java.util.Set; // <<< Import Set

/**
 * Controller สำหรับจัดการคำขอที่เกี่ยวกับ Product
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * แสดงรายการสินค้าทั้งหมด
     */
    @GetMapping("")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("productList", products);
        return "product-list";
    }

    /**
     * แสดงฟอร์มเพิ่มสินค้า
     */
    @GetMapping("/new")
    public String showAddProductForm(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        return "product-form";
    }

    /**
     * บันทึกสินค้าใหม่
     */
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product) {
        productService.createProduct(product);
        return "redirect:/products";
    }

    /**
     * แสดงฟอร์มแก้ไขสินค้า
     */
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") String id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "product-form";
    }

    /**
     * อัปเดตสินค้า
     */
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") String id, @ModelAttribute("product") Product product) {
        productService.updateProduct(id, product);
        return "redirect:/products";
    }

    /**
     * ลบสินค้า
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") String id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    // --- API Endpoint สำหรับ JavaScript ---

    /**
     * API Endpoint สำหรับดึง Variants ของ Product ที่กำหนด (สำหรับ JavaScript เรียกใช้)
     * @param productId รหัส Product ที่ส่งมาจาก JavaScript
     * @return Set ของ ProductVariant ในรูปแบบ JSON
     */
    @GetMapping("/variants/{productId}") // URL เช่น /products/variants/P-001
    @ResponseBody // <<< บอก Spring ว่าให้ส่งข้อมูลกลับเป็น JSON ไม่ใช่ชื่อ HTML
    public ResponseEntity<Set<ProductVariant>> getVariantsByProductId(@PathVariable String productId) {
        Optional<Product> productOpt = productService.getProductById(productId);

        if (productOpt.isPresent()) {
            // ดึง Set<ProductVariant> ออกมา (ต้องแน่ใจว่าโหลดมาแล้ว หรือ FetchType ไม่ใช่ LAZY ถ้าจะใช้ทันที)
            // หรืออาจจะต้องเขียน query เพิ่มใน Service/Repository เพื่อดึงเฉพาะ Variant
             Set<ProductVariant> variants = productOpt.get().getVariants();
             // *** สำคัญ: เพื่อป้องกัน Loop ตอนแปลงเป็น JSON อาจจะต้อง Clear relationship ฝั่งกลับ (product ใน variant) ***
             // variants.forEach(v -> v.setProduct(null)); // << อาจจะต้องทำแบบนี้ หรือใช้ DTO แทน

            return ResponseEntity.ok(variants); // ส่งข้อมูล Variants กลับไป (HTTP Status 200 OK)
        } else {
            return ResponseEntity.ok(Collections.emptySet()); // ส่ง Set ว่างกลับไป
        }
    }
}