package com.furnitureshop.furniture_erp_system;

import com.furnitureshop.furniture_erp_system.model.*;
import com.furnitureshop.furniture_erp_system.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn; // <<< (1) Import ตัวนี้เพิ่ม
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate; 
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List; 

@Component
@DependsOn("entityManagerFactory") // <<< (2) Annotation สำคัญ: รอให้ตารางสร้างเสร็จก่อน
public class DataInitializer implements CommandLineRunner {

    @Autowired 
    private PasswordEncoder passwordEncoder;

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private DeliveryZoneRepository deliveryZoneRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SupplierRepository supplierRepository; 
    @Autowired private PurchaseOrderRepository purchaseOrderRepository; 
    @Autowired private PurchaseOrderItemRepository purchaseOrderItemRepository; 

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("----- Initializing Sample Data -----");

        // --- 1. สร้าง Category ---
        Category catLivingRoom = createCategory("CAT-LR", "ห้องนั่งเล่น", "Room");
        Category catSofa = createCategory("CAT-SOFA", "โซฟา", "Type");
        Category catTable = createCategory("CAT-TABLE", "โต๊ะ", "Type"); 
        Category catChair = createCategory("CAT-CHAIR", "เก้าอี้", "Type"); 

        // --- 2. สร้าง Product ---
        Product sofaClassic = createProduct("P-001", "โซฟา The Classic", "โซฟา 3 ที่นั่ง สไตล์วินเทจ");
        Product tableModern = createProduct("P-002", "โต๊ะกาแฟ Modern", "โต๊ะกลาง ท็อปหินอ่อน"); 
        Product chairErg = createProduct("P-003", "เก้าอี้ทานอาหาร Erg", "เก้าอี้ดีไซน์โมเดิร์น"); 


        // --- 3. Link Product to Category ---
        linkProductToCategory(sofaClassic, catLivingRoom);
        linkProductToCategory(sofaClassic, catSofa);
        linkProductToCategory(tableModern, catLivingRoom); 
        linkProductToCategory(tableModern, catTable); 
        linkProductToCategory(chairErg, catChair); 

        // --- 4. สร้าง Product Variant ---
        ProductVariant variantGrey = createVariant("V-001", sofaClassic, "SKU-CLA-GRY", "{\"สี\": \"เทา\", \"วัสดุ\": \"ผ้า\"}", new BigDecimal("15900.00"));
        ProductVariant variantBlue = createVariant("V-002", sofaClassic, "SKU-CLA-BLU", "{\"สี\": \"น้ำเงิน\", \"วัสดุ\": \"หนัง\"}", new BigDecimal("17900.00"));
        ProductVariant variantWhiteTable = createVariant("V-003", tableModern, "SKU-MOD-WHT", "{\"สี\": \"ขาว\"}", new BigDecimal("8500.00")); 
        ProductVariant variantBlackTable = createVariant("V-004", tableModern, "SKU-MOD-BLK", "{\"สี\": \"ดำ\"}", new BigDecimal("8700.00")); 
        ProductVariant variantWhiteChair = createVariant("V-005", chairErg, "SKU-ERG-WHT", "{\"สี\": \"ขาว\", \"ขา\": \"ไม้\"}", new BigDecimal("2500.00")); 
        ProductVariant variantGreyChair = createVariant("V-006", chairErg, "SKU-ERG-GRY", "{\"สี\": \"เทา\", \"ขา\": \"เหล็ก\"}", new BigDecimal("2750.00")); 


        // --- 5. สร้าง Inventory ---
        createInventory(variantGrey, 10);
        createInventory(variantBlue, 5);
        createInventory(variantWhiteTable, 8); 


        // --- 6. สร้าง Delivery Zones ---
        DeliveryZone zone1 = createZone("Z-01", "กทม. ชั้นใน", new BigDecimal("300.00"));
        DeliveryZone zone2 = createZone("Z-02", "ปริมณฑล", new BigDecimal("500.00"));
        DeliveryZone zone3 = createZone("Z-03", "กทม. รอบนอก", new BigDecimal("400.00")); 

        // --- 7. สร้าง Customers ---
        createCustomer("C-001", "สมชาย ใจดี", "081-111-2222", "123 สุขุมวิท กทม.", zone1);
        createCustomer("C-002", "สมหญิง มีสุข", "082-333-4444", "456 แจ้งวัฒนะ นนทบุรี", zone2);
        createCustomer("C-003", "วิชัย มานะ", "083-555-6666", "789 รามอินทรา กทม.", zone3); 

        // --- 8. สร้าง Users ---
        createUser("U-001", "admin", "pass123", "Admin");
        createUser("U-002", "sales01", "pass123", "Sales");
        createUser("U-003", "stock01", "pass123", "Stock");
        createUser("U-004", "delivery01", "pass123", "Delivery");
        createUser("U-005", "sales02", "pass123", "Sales");

        // --- 9. สร้าง Suppliers ---
        Supplier supplier1 = createSupplier("S-001", "บจก. ไทยเฟอร์นิเจอร์", "02-111-2222");
        Supplier supplier2 = createSupplier("S-002", "หจก. ดีไซน์วู้ด", "034-555-6666");

        // --- 10. สร้าง Purchase Orders และ Items ---
        PurchaseOrder po1 = createPO("PO-001", supplier1, LocalDate.of(2025, 10, 1), "Received");
        createPOItem(po1, variantGrey, 10, new BigDecimal("8000.00"));
        createPOItem(po1, variantBlue, 5, new BigDecimal("9500.00"));
        createPOItem(po1, variantWhiteTable, 8, new BigDecimal("4200.00"));

        PurchaseOrder po2 = createPO("PO-002", supplier2, LocalDate.of(2025, 10, 15), "Ordered");
        createPOItem(po2, variantWhiteChair, 20, new BigDecimal("1500.00"));
        createPOItem(po2, variantGreyChair, 15, new BigDecimal("1650.00"));


        System.out.println("----- Sample Data Initialized -----");
    }

    // --- Helper Methods (สร้างใหม่/บันทึกเท่านั้น) ---

    private Category createCategory(String id, String name, String type) { 
        Category c=new Category(); 
        c.setCategoryID(id); c.setName(name); c.setType(type); 
        System.out.println("Creating Cat: "+name); 
        return categoryRepository.save(c); 
    }
    
    private Product createProduct(String id, String name, String description) { 
        Product p=new Product(); 
        p.setProductID(id); p.setName(name); p.setDescription(description); 
        System.out.println("Creating Prod: "+name); 
        return productRepository.save(p); 
    }
    
    private void linkProductToCategory(Product product, Category category) { 
         // เนื่องจากใช้ create ข้อมูล Product จะต้องถูก Load/Merge กลับเข้า Session
         Product freshProduct = productRepository.findById(product.getProductID()).orElse(product); 
         if (freshProduct.getCategories() == null) {
            freshProduct.setCategories(new HashSet<>());
         }
         freshProduct.getCategories().add(category);
         System.out.println("Linking "+product.getName()+" to "+category.getName()); 
         productRepository.save(freshProduct);
    }
    
    private ProductVariant createVariant(String id, Product product, String sku, String attributes, BigDecimal price) { 
        ProductVariant v=new ProductVariant(); 
        v.setVariantID(id); v.setProduct(product); v.setSkuCode(sku); v.setAttributes(attributes); v.setUnitPrice(price); 
        System.out.println("Creating Var: "+sku); 
        return variantRepository.save(v); 
    }
    
    private Inventory createInventory(ProductVariant variant, int onHandQty) { 
        Inventory i=new Inventory(); 
        i.setProductVariant(variant); i.setQuantityOnHand(onHandQty); i.setQuantityReserved(0); 
        System.out.println("Creating Inv for: "+variant.getSkuCode()+" ("+onHandQty+")"); 
        return inventoryRepository.save(i); 
    }
    
    private DeliveryZone createZone(String id, String name, BigDecimal fee) { 
        DeliveryZone z=new DeliveryZone(); 
        z.setZoneID(id); z.setZoneName(name); z.setDeliveryFee(fee); 
        System.out.println("Creating Zone: "+name); 
        return deliveryZoneRepository.save(z); 
    }
    
    private Customer createCustomer(String id, String name, String phone, String address, DeliveryZone zone) { 
        Customer c=new Customer(); 
        c.setCustomerID(id); c.setName(name); c.setPhone(phone); c.setAddress(address); c.setDeliveryZone(zone); 
        System.out.println("Creating Cust: "+name); 
        return customerRepository.save(c); 
    }
    
    private User createUser(String id, String username, String plainPassword, String role) {
        User u = new User();
        u.setUserID(id);
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(plainPassword)); 
        u.setRole(role);
        System.out.println("Creating User: " + username + " (Role: " + role + ")");
        return userRepository.save(u);
    }

    private Supplier createSupplier(String id, String name, String contact) {
        Supplier newSup = new Supplier();
        newSup.setSupplierID(id);
        newSup.setName(name);
        newSup.setContact(contact);
        System.out.println("Creating Supplier: " + name);
        return supplierRepository.save(newSup);
    }

    private PurchaseOrder createPO(String id, Supplier supplier, LocalDate date, String status) {
        PurchaseOrder newPO = new PurchaseOrder();
        newPO.setPoID(id);
        newPO.setSupplier(supplier);
        newPO.setOrderDate(date);
        newPO.setStatus(status);
        System.out.println("Creating PO: " + id);
        return purchaseOrderRepository.save(newPO);
    }

    private PurchaseOrderItem createPOItem(PurchaseOrder po, ProductVariant variant, int quantity, BigDecimal cost) {
        PurchaseOrderItem newItem = new PurchaseOrderItem();
        newItem.setPurchaseOrder(po);
        newItem.setProductVariant(variant);
        newItem.setQuantity(quantity);
        newItem.setCostPerUnit(cost);
        System.out.println("Creating PO Item for " + po.getPoID() + ": " + variant.getSkuCode());
        return purchaseOrderItemRepository.save(newItem);
    }
}