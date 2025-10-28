package com.furnitureshop.furniture_erp_system.service;

import com.furnitureshop.furniture_erp_system.model.Category;
import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.ProductVariant;
import com.furnitureshop.furniture_erp_system.repository.CategoryRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    // --- Logic การจัดการ Product ---

    /**
     * ดึงข้อมูล Product ทั้งหมด
     * @return List ของ Product
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * ค้นหา Product ตาม ID
     * @param productId รหัส Product
     * @return Optional<Product> (อาจจะเจอหรือไม่เจอ)
     */
    public Optional<Product> getProductById(String productId) {
        return productRepository.findById(productId);
    }

    /**
     * สร้าง Product ใหม่
     * @param product ข้อมูล Product ที่จะสร้าง
     * @return Product ที่บันทึกแล้ว
     */
    @Transactional // ทำให้การทำงานทั้งหมดนี้ ถ้าผิดพลาดจะ Rollback
    public Product createProduct(Product product) {
        // (Optional) สามารถเพิ่ม Logic การตรวจสอบข้อมูลซ้ำก่อนบันทึกได้
        // เช่น ตรวจสอบว่า productID ซ้ำหรือไม่
        return productRepository.save(product);
    }

    /**
     * อัปเดตข้อมูล Product ที่มีอยู่ (อัปเดตเฉพาะ Name และ Description)
     * @param productId รหัส Product ที่จะอัปเดต
     * @param productDetails ข้อมูลใหม่จากฟอร์ม
     * @return Product ที่อัปเดตแล้ว
     * @throws RuntimeException ถ้าหา Product ไม่เจอ
     */
    @Transactional // ทำให้การทำงานทั้งหมดนี้ ถ้าผิดพลาดจะ Rollback
    public Product updateProduct(String productId, Product productDetails) {
        // 1. ค้นหา Product เดิมจาก ID
        Product existingProduct = productRepository.findById(productId)
                // ถ้าหาไม่เจอ ให้โยน Error ออกไป
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // 2. อัปเดตเฉพาะฟิลด์ที่ต้องการ (Name, Description)
        //    (เราไม่ควรให้แก้ไข ProductID)
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());

        // 3. บันทึก Product ที่อัปเดตแล้ว
        return productRepository.save(existingProduct);
    }

    /**
     * ลบ Product ตาม ID
     * @param productId รหัส Product ที่จะลบ
     */
    @Transactional
    public void deleteProduct(String productId) {
        // (สำคัญ) ในอนาคต ควรตรวจสอบก่อนว่า Product นี้มี Variants
        // หรือถูกอ้างอิงใน SO/PO หรือไม่ ก่อนที่จะอนุญาตให้ลบ
        // ถ้ามีการอ้างอิงอยู่ อาจจะต้องป้องกันการลบ หรือใช้วิธี Soft Delete แทน
        Product product = productRepository.findById(productId)
                 .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        productRepository.delete(product);
    }

    // --- Logic การจัดการ Category ---

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // --- Logic การจัดการ ProductVariant (SKU) ---

    public Optional<ProductVariant> getVariantById(String variantId) {
        return variantRepository.findById(variantId);
    }

    @Transactional
    public ProductVariant createVariant(String productId, ProductVariant variant) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        variant.setProduct(product);
        return variantRepository.save(variant);
    }

    // --- Logic การเชื่อม Product กับ Category (Many-to-Many) ---

    @Transactional
    public Product addCategoryToProduct(String productId, String categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        product.getCategories().add(category);
        return productRepository.save(product);
    }

    @Transactional
    public Product removeCategoryFromProduct(String productId, String categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        product.getCategories().remove(category);
        return productRepository.save(product);
    }
}