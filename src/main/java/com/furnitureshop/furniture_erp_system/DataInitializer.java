package com.furnitureshop.furniture_erp_system; // อยู่ในแพ็กเกจหลัก

import com.furnitureshop.furniture_erp_system.model.*; // Import โมเดลทั้งหมด
import com.furnitureshop.furniture_erp_system.repository.*; // Import repositories ทั้งหมด

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * คลาสนี้จะทำงานอัตโนมัติเมื่อแอปเริ่มทำงาน
 * เพื่อสร้างข้อมูลตัวอย่างลงในฐานข้อมูล (ถ้ายังไม่มี)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    // --- ฉีด Repositories ทั้งหมด ---
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private DeliveryZoneRepository deliveryZoneRepository; // <<< เพิ่ม
    @Autowired private CustomerRepository customerRepository; // <<< เพิ่ม
    @Autowired private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("----- Initializing Sample Data -----");

        // --- 1. สร้าง Category ---
        Category catLivingRoom = createCategoryIfNotExists("CAT-LR", "ห้องนั่งเล่น", "Room");
        Category catSofa = createCategoryIfNotExists("CAT-SOFA", "โซฟา", "Type");

        // --- 2. สร้าง Product ---
        Product sofaClassic = createProductIfNotExists("P-001", "โซฟา The Classic", "โซฟา 3 ที่นั่ง สไตล์วินเทจ");

        // --- 3. เชื่อม Product กับ Category ---
        linkProductToCategoryIfNotLinked(sofaClassic, catLivingRoom);
        linkProductToCategoryIfNotLinked(sofaClassic, catSofa);

        // --- 4. สร้าง Product Variants (SKU) ---
        ProductVariant variantGrey = createVariantIfNotExists(
            "V-001", sofaClassic, "SKU-CLA-GRY",
            "{\"สี\": \"เทา\", \"วัสดุ\": \"ผ้า\"}", new BigDecimal("15900.00")
        );
        ProductVariant variantBlue = createVariantIfNotExists(
            "V-002", sofaClassic, "SKU-CLA-BLU",
            "{\"สี\": \"น้ำเงิน\", \"วัสดุ\": \"หนัง\"}", new BigDecimal("17900.00")
        );

        // --- 5. สร้าง Inventory Record ---
        createInventoryIfNotExists(variantGrey, 10);
        createInventoryIfNotExists(variantBlue, 5);

        // --- 6. สร้าง Delivery Zones ---
        DeliveryZone zone1 = createZoneIfNotExists("Z-01", "กทม. ชั้นใน", new BigDecimal("300.00"));
        DeliveryZone zone2 = createZoneIfNotExists("Z-02", "ปริมณฑล", new BigDecimal("500.00"));

        // --- 7. สร้าง Customers ---
        createCustomerIfNotExists("C-001", "สมชาย ใจดี", "081-111-2222", "123 สุขุมวิท กทม.", zone1); // <<< เชื่อม Zone
        createCustomerIfNotExists("C-002", "สมหญิง มีสุข", "082-333-4444", "456 แจ้งวัฒนะ นนทบุรี", zone2); // <<< เชื่อม Zone

        System.out.println("----- Sample Data Initialized -----");
        
     // --- 8. สร้าง Users ตัวอย่าง ---
        createUserIfNotExists("U-001", "admin", "password", "Admin");
        createUserIfNotExists("U-002", "sales01", "password", "Sales"); // <<< สร้าง U-002
        createUserIfNotExists("U-003", "stock01", "password", "Stock");
        
       
    }

    // --- Helper Methods (ฟังก์ชันช่วย) ---

    private Category createCategoryIfNotExists(String id, String name, String type) {
        return categoryRepository.findById(id).orElseGet(() -> {
            Category newCat = new Category();
            newCat.setCategoryID(id);
            newCat.setName(name);
            newCat.setType(type);
            System.out.println("Creating Category: " + name);
            return categoryRepository.save(newCat);
        });
    }

    private Product createProductIfNotExists(String id, String name, String description) {
        return productRepository.findById(id).orElseGet(() -> {
            Product newProd = new Product();
            newProd.setProductID(id);
            newProd.setName(name);
            newProd.setDescription(description);
            System.out.println("Creating Product: " + name);
            return productRepository.save(newProd);
        });
    }

     private void linkProductToCategoryIfNotLinked(Product product, Category category) {
         Product freshProduct = productRepository.findById(product.getProductID()).orElse(product);
         boolean alreadyLinked = freshProduct.getCategories().stream()
                                     .anyMatch(cat -> cat.getCategoryID().equals(category.getCategoryID()));
        if (!alreadyLinked) {
            freshProduct.getCategories().add(category);
            System.out.println("Linking Product '" + product.getName() + "' to Category '" + category.getName() + "'");
            productRepository.save(freshProduct);
        }
    }

    private ProductVariant createVariantIfNotExists(String id, Product product, String sku, String attributes, BigDecimal price) {
        return variantRepository.findById(id).orElseGet(() -> {
            ProductVariant newVar = new ProductVariant();
            newVar.setVariantID(id);
            newVar.setProduct(product);
            newVar.setSkuCode(sku);
            newVar.setAttributes(attributes);
            newVar.setUnitPrice(price);
            System.out.println("Creating Variant: " + sku);
            return variantRepository.save(newVar);
        });
    }

    private Inventory createInventoryIfNotExists(ProductVariant variant, int onHandQty) {
        return inventoryRepository.findByProductVariant_VariantID(variant.getVariantID()).orElseGet(() -> {
            Inventory newInv = new Inventory();
            newInv.setProductVariant(variant);
            newInv.setQuantityOnHand(onHandQty);
            newInv.setQuantityReserved(0);
            System.out.println("Creating Inventory for: " + variant.getSkuCode() + " (OnHand: " + onHandQty + ")");
            return inventoryRepository.save(newInv);
        });
    }

    // <<< เพิ่ม Helper สำหรับ Zone >>>
    private DeliveryZone createZoneIfNotExists(String id, String name, BigDecimal fee) {
        return deliveryZoneRepository.findById(id).orElseGet(() -> {
            DeliveryZone newZone = new DeliveryZone();
            newZone.setZoneID(id);
            newZone.setZoneName(name);
            newZone.setDeliveryFee(fee);
            System.out.println("Creating Delivery Zone: " + name);
            return deliveryZoneRepository.save(newZone);
        });
    }

    // <<< เพิ่ม Helper สำหรับ Customer >>>
    private Customer createCustomerIfNotExists(String id, String name, String phone, String address, DeliveryZone zone) {
        return customerRepository.findById(id).orElseGet(() -> {
            Customer newCust = new Customer();
            newCust.setCustomerID(id);
            newCust.setName(name);
            newCust.setPhone(phone);
            newCust.setAddress(address);
            newCust.setDeliveryZone(zone); // <<< เชื่อม Zone
            System.out.println("Creating Customer: " + name);
            return customerRepository.save(newCust);
        });
    }
    
    private User createUserIfNotExists(String id, String username, String password, String role) {
        return userRepository.findById(id).orElseGet(() -> {
            User newUser = new User();
            newUser.setUserID(id);
            newUser.setUsername(username);
            // *** สำคัญ: ต้องเข้ารหัส Password ก่อนเก็บจริง ***
            // ใช้ BCryptPasswordEncoder (ต้อง Autowired เข้ามา)
            // newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setPasswordHash(password); // <<< ใช้แบบไม่เข้ารหัสไปก่อน (เพื่อทดสอบ)
            newUser.setRole(role);
            System.out.println("Creating User: " + username);
            return userRepository.save(newUser);
        });
    }
}