package com.furnitureshop.furniture_erp_system;

import com.furnitureshop.furniture_erp_system.model.*;
import com.furnitureshop.furniture_erp_system.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder; // <<< Import ที่เพิ่มเข้ามา

import java.math.BigDecimal;
import java.time.LocalDate; 
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List; 

@Component
public class DataInitializer implements CommandLineRunner {

    // === ฉีด PasswordEncoder เข้ามา ===
    @Autowired 
    private PasswordEncoder passwordEncoder;

    // --- Autowired Repositories (เพิ่ม PO Repos) ---
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private DeliveryZoneRepository deliveryZoneRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SupplierRepository supplierRepository; // <<< เพิ่ม
    @Autowired private PurchaseOrderRepository purchaseOrderRepository; // <<< เพิ่ม
    @Autowired private PurchaseOrderItemRepository purchaseOrderItemRepository; // <<< เพิ่ม

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("----- Initializing Sample Data -----");

        // --- 1-5. สร้าง Category, Product, Link, Variant, Inventory (เหมือนเดิม) ---
        Category catLivingRoom = createCategoryIfNotExists("CAT-LR", "ห้องนั่งเล่น", "Room");
        Category catSofa = createCategoryIfNotExists("CAT-SOFA", "โซฟา", "Type");
        Category catTable = createCategoryIfNotExists("CAT-TABLE", "โต๊ะ", "Type"); // เพิ่ม
        Category catChair = createCategoryIfNotExists("CAT-CHAIR", "เก้าอี้", "Type"); // เพิ่ม

        Product sofaClassic = createProductIfNotExists("P-001", "โซฟา The Classic", "โซฟา 3 ที่นั่ง สไตล์วินเทจ");
        Product tableModern = createProductIfNotExists("P-002", "โต๊ะกาแฟ Modern", "โต๊ะกลาง ท็อปหินอ่อน"); // เพิ่ม
        Product chairErg = createProductIfNotExists("P-003", "เก้าอี้ทานอาหาร Erg", "เก้าอี้ดีไซน์โมเดิร์น"); // เพิ่ม


        linkProductToCategoryIfNotLinked(sofaClassic, catLivingRoom);
        linkProductToCategoryIfNotLinked(sofaClassic, catSofa);
        linkProductToCategoryIfNotLinked(tableModern, catLivingRoom); // เพิ่ม link
        linkProductToCategoryIfNotLinked(tableModern, catTable); // เพิ่ม link
        linkProductToCategoryIfNotLinked(chairErg, catChair); // เพิ่ม link
        // linkProductToCategoryIfNotLinked(chairErg, catDiningRoom); // ถ้ามีหมวดหมู่อื่นๆ

        ProductVariant variantGrey = createVariantIfNotExists("V-001", sofaClassic, "SKU-CLA-GRY", "{\"สี\": \"เทา\", \"วัสดุ\": \"ผ้า\"}", new BigDecimal("15900.00"));
        ProductVariant variantBlue = createVariantIfNotExists("V-002", sofaClassic, "SKU-CLA-BLU", "{\"สี\": \"น้ำเงิน\", \"วัสดุ\": \"หนัง\"}", new BigDecimal("17900.00"));
        ProductVariant variantWhiteTable = createVariantIfNotExists("V-003", tableModern, "SKU-MOD-WHT", "{\"สี\": \"ขาว\"}", new BigDecimal("8500.00")); // เพิ่ม
        ProductVariant variantBlackTable = createVariantIfNotExists("V-004", tableModern, "SKU-MOD-BLK", "{\"สี\": \"ดำ\"}", new BigDecimal("8700.00")); // เพิ่ม
        ProductVariant variantWhiteChair = createVariantIfNotExists("V-005", chairErg, "SKU-ERG-WHT", "{\"สี\": \"ขาว\", \"ขา\": \"ไม้\"}", new BigDecimal("2500.00")); // เพิ่ม
        ProductVariant variantGreyChair = createVariantIfNotExists("V-006", chairErg, "SKU-ERG-GRY", "{\"สี\": \"เทา\", \"ขา\": \"เหล็ก\"}", new BigDecimal("2750.00")); // เพิ่ม


        createInventoryIfNotExists(variantGrey, 10);
        createInventoryIfNotExists(variantBlue, 5);
        createInventoryIfNotExists(variantWhiteTable, 8); // เพิ่ม
        // variantBlackTable ไม่มีสต็อก
        // variantWhiteChair ไม่มีสต็อก
        // variantGreyChair ไม่มีสต็อก


        // --- 6. สร้าง Delivery Zones (เหมือนเดิม) ---
        DeliveryZone zone1 = createZoneIfNotExists("Z-01", "กทม. ชั้นใน", new BigDecimal("300.00"));
        DeliveryZone zone2 = createZoneIfNotExists("Z-02", "ปริมณฑล", new BigDecimal("500.00"));
        DeliveryZone zone3 = createZoneIfNotExists("Z-03", "กทม. รอบนอก", new BigDecimal("400.00")); // เพิ่ม

        // --- 7. สร้าง Customers (เหมือนเดิม) ---
        createCustomerIfNotExists("C-001", "สมชาย ใจดี", "081-111-2222", "123 สุขุมวิท กทม.", zone1);
        createCustomerIfNotExists("C-002", "สมหญิง มีสุข", "082-333-4444", "456 แจ้งวัฒนะ นนทบุรี", zone2);
        createCustomerIfNotExists("C-003", "วิชัย มานะ", "083-555-6666", "789 รามอินทรา กทม.", zone3); // เพิ่ม

        // --- 8. สร้าง Users (แก้ไขให้ใช้รหัสผ่าน "pass123") ---
        createUserIfNotExists("U-001", "admin", "pass123", "Admin");
        createUserIfNotExists("U-002", "sales01", "pass123", "Sales");
        createUserIfNotExists("U-003", "stock01", "pass123", "Stock");
        createUserIfNotExists("U-004", "delivery01", "pass123", "Delivery");
        createUserIfNotExists("U-005", "sales02", "pass123", "Sales");

        // --- 9. สร้าง Suppliers (ถ้ายังไม่มี) ---
        Supplier supplier1 = createSupplierIfNotExists("S-001", "บจก. ไทยเฟอร์นิเจอร์", "02-111-2222");
        Supplier supplier2 = createSupplierIfNotExists("S-002", "หจก. ดีไซน์วู้ด", "034-555-6666");

        // --- 10. สร้าง Purchase Orders และ Items (ถ้ายังไม่มี) ---
        PurchaseOrder po1 = createPOIfNotExists("PO-001", supplier1, LocalDate.of(2025, 10, 1), "Received");
        createPOItemIfNotExists(po1, variantGrey, 10, new BigDecimal("8000.00"));
        createPOItemIfNotExists(po1, variantBlue, 5, new BigDecimal("9500.00"));
        createPOItemIfNotExists(po1, variantWhiteTable, 8, new BigDecimal("4200.00"));

        PurchaseOrder po2 = createPOIfNotExists("PO-002", supplier2, LocalDate.of(2025, 10, 15), "Ordered");
        createPOItemIfNotExists(po2, variantWhiteChair, 20, new BigDecimal("1500.00"));
        createPOItemIfNotExists(po2, variantGreyChair, 15, new BigDecimal("1650.00"));


        System.out.println("----- Sample Data Initialized -----");
    }

    // --- Helper Methods (ฟังก์ชันช่วย - เพิ่ม PO Helpers) ---

    // (Helper สำหรับ Category, Product, Link, Variant, Inventory, Zone, Customer, User เหมือนเดิม)
    private Category createCategoryIfNotExists(String id, String name, String type) { /* ... โค้ดเดิม ... */ return categoryRepository.findById(id).orElseGet(() -> { Category c=new Category(); c.setCategoryID(id); c.setName(name); c.setType(type); System.out.println("Creating Cat: "+name); return categoryRepository.save(c); }); }
    private Product createProductIfNotExists(String id, String name, String description) { /* ... โค้ดเดิม ... */ return productRepository.findById(id).orElseGet(() -> { Product p=new Product(); p.setProductID(id); p.setName(name); p.setDescription(description); System.out.println("Creating Prod: "+name); return productRepository.save(p); }); }
    private void linkProductToCategoryIfNotLinked(Product product, Category category) { /* ... โค้ดเดิม ... */ Product freshProduct = productRepository.findById(product.getProductID()).orElse(product); boolean linked = freshProduct.getCategories().stream().anyMatch(cat -> cat.getCategoryID().equals(category.getCategoryID())); if (!linked) { freshProduct.getCategories().add(category); System.out.println("Linking "+product.getName()+" to "+category.getName()); productRepository.save(freshProduct); } }
    private ProductVariant createVariantIfNotExists(String id, Product product, String sku, String attributes, BigDecimal price) { /* ... โค้ดเดิม ... */ return variantRepository.findById(id).orElseGet(() -> { ProductVariant v=new ProductVariant(); v.setVariantID(id); v.setProduct(product); v.setSkuCode(sku); v.setAttributes(attributes); v.setUnitPrice(price); System.out.println("Creating Var: "+sku); return variantRepository.save(v); }); }
    private Inventory createInventoryIfNotExists(ProductVariant variant, int onHandQty) { /* ... โค้ดเดิม ... */ return inventoryRepository.findByProductVariant_VariantID(variant.getVariantID()).orElseGet(() -> { Inventory i=new Inventory(); i.setProductVariant(variant); i.setQuantityOnHand(onHandQty); i.setQuantityReserved(0); System.out.println("Creating Inv for: "+variant.getSkuCode()+" ("+onHandQty+")"); return inventoryRepository.save(i); }); }
    private DeliveryZone createZoneIfNotExists(String id, String name, BigDecimal fee) { /* ... โค้ดเดิม ... */ return deliveryZoneRepository.findById(id).orElseGet(() -> { DeliveryZone z=new DeliveryZone(); z.setZoneID(id); z.setZoneName(name); z.setDeliveryFee(fee); System.out.println("Creating Zone: "+name); return deliveryZoneRepository.save(z); }); }
    private Customer createCustomerIfNotExists(String id, String name, String phone, String address, DeliveryZone zone) { /* ... โค้ดเดิม ... */ return customerRepository.findById(id).orElseGet(() -> { Customer c=new Customer(); c.setCustomerID(id); c.setName(name); c.setPhone(phone); c.setAddress(address); c.setDeliveryZone(zone); System.out.println("Creating Cust: "+name); return customerRepository.save(c); }); }
    
    // <<< อัปเดต Helper Method นี้ >>>
    private User createUserIfNotExists(String id, String username, String plainPassword, String role) {
        // ค้นหาด้วย Username
        return userRepository.findByUsername(username).orElseGet(() -> { 
            User u = new User();
            u.setUserID(id);
            u.setUsername(username);
            
            // *** เข้ารหัสผ่านก่อน Save ***
            u.setPasswordHash(passwordEncoder.encode(plainPassword)); // ใช้ .encode()
            
            u.setRole(role);
            System.out.println("Creating User: " + username + " (Role: " + role + ")");
            return userRepository.save(u);
        });
    }

    // <<< เพิ่ม Helper สำหรับ Supplier >>>
    private Supplier createSupplierIfNotExists(String id, String name, String contact) {
        return supplierRepository.findById(id).orElseGet(() -> {
            Supplier newSup = new Supplier();
            newSup.setSupplierID(id);
            newSup.setName(name);
            newSup.setContact(contact);
            System.out.println("Creating Supplier: " + name);
            return supplierRepository.save(newSup);
        });
    }

    // <<< เพิ่ม Helper สำหรับ PurchaseOrder >>>
    private PurchaseOrder createPOIfNotExists(String id, Supplier supplier, LocalDate date, String status) {
        return purchaseOrderRepository.findById(id).orElseGet(() -> {
            PurchaseOrder newPO = new PurchaseOrder();
            newPO.setPoID(id);
            newPO.setSupplier(supplier);
            newPO.setOrderDate(date);
            newPO.setStatus(status);
            System.out.println("Creating PO: " + id);
            return purchaseOrderRepository.save(newPO);
        });
    }

    // <<< เพิ่ม Helper สำหรับ PurchaseOrderItem >>>
    private PurchaseOrderItem createPOItemIfNotExists(PurchaseOrder po, ProductVariant variant, int quantity, BigDecimal cost) {
        // หา Item ที่มี PO ID และ Variant ID ตรงกัน (ป้องกันการสร้างซ้ำ)
        List<PurchaseOrderItem> existingItems = purchaseOrderItemRepository.findByPurchaseOrderAndProductVariant(po, variant); // <<< ต้องเพิ่ม Method นี้ใน Repo
        if (existingItems.isEmpty()) {
            PurchaseOrderItem newItem = new PurchaseOrderItem();
            newItem.setPurchaseOrder(po);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);
            newItem.setCostPerUnit(cost);
            System.out.println("Creating PO Item for " + po.getPoID() + ": " + variant.getSkuCode());
            return purchaseOrderItemRepository.save(newItem);
        }
        return existingItems.get(0); // คืนค่า Item ที่มีอยู่แล้ว
    }
}