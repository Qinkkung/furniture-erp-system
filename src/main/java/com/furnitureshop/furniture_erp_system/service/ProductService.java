package com.furnitureshop.furniture_erp_system.service;

import com.furnitureshop.furniture_erp_system.dto.ProductVariantDropdownDto; // <<< Import DTO
import com.furnitureshop.furniture_erp_system.model.Category;
import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.ProductVariant;
import com.furnitureshop.furniture_erp_system.repository.CategoryRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository;

import org.hibernate.Hibernate; // <<< Import Hibernate
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // Import BigDecimal (เผื่อใช้ในอนาคต)
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // <<< Import Collectors

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    // --- Logic การจัดการ Product ---

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String productId) {
        // Optional: Implement findByIdWithCategoriesAndVariants if needed for detail pages
        return productRepository.findById(productId);
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(String productId, Product productDetails) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(String productId) {
        // IMPORTANT: Add checks here later to prevent deleting products with active variants/stock/orders
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

    /**
     * ดึงข้อมูล Variant ทั้งหมดสำหรับ Dropdown (ใช้ DTO)
     * พร้อมบังคับโหลดชื่อ Product เพื่อแก้ปัญหา Lazy Loading ใน View/JS
     * @return List ของ ProductVariantDropdownDto
     */
    @Transactional(readOnly = true) // ใช้ Transaction เพื่อให้โหลด Lazy ได้
    public List<ProductVariantDropdownDto> getAllVariantsForDropdown() {
        List<ProductVariant> variants = variantRepository.findAll();
        // *** บังคับโหลด Product และใส่ชื่อลง DTO ***
        return variants.stream()
                .map(variant -> {
                    String productName = "Unknown Product"; // Default name
                    if (variant.getProduct() != null) {
                        try {
                            // บังคับโหลด Proxy ของ Product (ถ้ายังเป็น Proxy)
                            Hibernate.initialize(variant.getProduct());
                            productName = variant.getProduct().getName(); // ดึงชื่อ Product
                        } catch (org.hibernate.LazyInitializationException e) {
                            // Handle cases where session might be closed unexpectedly, though @Transactional should prevent this
                            System.err.println("LazyInitializationException fetching product name for variant " + variant.getVariantID() + ": " + e.getMessage());
                        }
                    }
                    return new ProductVariantDropdownDto(
                        variant.getVariantID(),
                        variant.getSkuCode(),
                        productName, // <<< ใช้ชื่อ Product ที่โหลดมา
                        variant.getAttributes(),
                        variant.getUnitPrice()
                    );
                 })
                .collect(Collectors.toList());
    }


    @Transactional
    public ProductVariant createVariant(String productId, ProductVariant variant) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        variant.setProduct(product);
        // (Optional) เช็ค SKU ซ้ำก่อน Save
        // Optional<ProductVariant> existingSku = variantRepository.findBySkuCode(variant.getSkuCode()); // ต้องเพิ่ม Method นี้ใน Repo
        // if(existingSku.isPresent()) { throw new RuntimeException("SKU Code already exists: " + variant.getSkuCode()); }
        return variantRepository.save(variant);
    }

    // --- Logic การเชื่อม Product กับ Category ---

    @Transactional
    public Product addCategoryToProduct(String productId, String categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
        // Ensure collections are initialized within the transaction
        Hibernate.initialize(product.getCategories());
        product.getCategories().add(category);
        return productRepository.save(product);
    }

    @Transactional
    public Product removeCategoryFromProduct(String productId, String categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
        // Ensure collections are initialized
        Hibernate.initialize(product.getCategories());
        product.getCategories().remove(category);
        return productRepository.save(product);
    }
    
    // === NEW: Helper Methods for ID Generation (Fix for Controller) ===

    /**
     * Helper Method สำหรับสร้าง Product ID ใหม่ (ใช้ใน ProductController)
     */
    public String generateNewProductId() {
        // NOTE: ใน Production ควรใช้ Sequence หรือ UUID ที่ปลอดภัยกว่า
        return "P-" + System.currentTimeMillis();
    }
    
    /**
     * Helper Method สำหรับสร้าง Variant ID ใหม่ (ใช้ใน ProductController)
     */
    public String generateNewVariantId(String productId) {
        // สร้าง ID โดยใช้ Product ID เป็นส่วนประกอบ (เช่น V-P-001-TIMESTAMP)
        return "V-" + productId.replace("P-", "") + "-" + System.currentTimeMillis();
    }
}