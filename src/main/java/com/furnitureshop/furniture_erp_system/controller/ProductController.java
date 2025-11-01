package com.furnitureshop.furniture_erp_system.controller;

import com.furnitureshop.furniture_erp_system.model.Category;
import com.furnitureshop.furniture_erp_system.model.Product;
import com.furnitureshop.furniture_erp_system.model.ProductVariant; 
import com.furnitureshop.furniture_erp_system.repository.CategoryRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductRepository;
import com.furnitureshop.furniture_erp_system.repository.ProductVariantRepository; 
import com.furnitureshop.furniture_erp_system.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional; 
import org.hibernate.Hibernate; 

import java.util.Collections;
import java.util.HashSet; 
import java.util.List;
import java.util.Map; 
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.furnitureshop.furniture_erp_system.dto.ProductVariantDropdownDto; 


@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository; 
    @Autowired private CategoryRepository categoryRepository; 
    @Autowired private ProductVariantRepository productVariantRepository; 

    @GetMapping("")
    @Transactional(readOnly = true) 
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        products.forEach(product -> Hibernate.initialize(product.getCategories()));
        Map<String, String> categoryNames = products.stream()
            .collect(Collectors.toMap(
                Product::getProductID,
                product -> product.getCategories().isEmpty() 
                            ? "N/A" 
                            : product.getCategories().iterator().next().getName()
            ));
        model.addAttribute("productList", products);
        model.addAttribute("categoryNames", categoryNames); 
        return "product-list";
    }

    @GetMapping("/new")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product()); 
        model.addAttribute("allCategories", categoryRepository.findAll());
        model.addAttribute("isNew", true); 
        return "product-form";
    }

    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true) 
    public String showEditProductForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Hibernate.initialize(product.getCategories()); 
            model.addAttribute("product", product);
            model.addAttribute("allCategories", categoryRepository.findAll());
            model.addAttribute("isNew", false); 
            return "product-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบสินค้า ID: " + id);
            return "redirect:/products";
        }
    }
    
    @PostMapping("/save/new")
    @Transactional
    public String saveNewProduct(@ModelAttribute Product product, 
                                 @RequestParam(value = "categoryIds", required = false) List<String> categoryIds, 
                                 RedirectAttributes redirectAttributes) {
        
        if (productRepository.existsById(product.getProductID())) {
            redirectAttributes.addFlashAttribute("errorMessage", "รหัสสินค้า '" + product.getProductID() + "' นี้มีอยู่แล้ว!");
            redirectAttributes.addFlashAttribute("product", product); 
            return "redirect:/products/new";
        }
        product.setCategories(new HashSet<>()); 
        if (categoryIds != null) {
            categoryIds.forEach(id -> {
                categoryRepository.findById(id).ifPresent(category -> product.getCategories().add(category));
            });
        }
        productRepository.save(product); 
        redirectAttributes.addFlashAttribute("successMessage", "สร้างสินค้า '" + product.getName() + "' สำเร็จ!");
        return "redirect:/products";
    }

    /**
     * [FIX] Method ใหม่สำหรับ "อัปเดตสินค้าเดิม" เท่านั้น
     * FIX: เพิ่ม Hibernate.initialize() เพื่อแก้ Lazy Loading Exception
     */
    @PostMapping("/save/update")
    @Transactional
    public String saveUpdateProduct(@ModelAttribute Product product, 
                                    @RequestParam(value = "categoryIds", required = false) List<String> categoryIds, 
                                    RedirectAttributes redirectAttributes) {

        Product existingProduct = productRepository.findById(product.getProductID())
            .orElseThrow(() -> new RuntimeException("Product not found ID: " + product.getProductID()));
            
        // *** (FIX) บังคับโหลด Categories เก่าก่อนที่จะ .clear() ***
        Hibernate.initialize(existingProduct.getCategories()); 
            
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        
        existingProduct.getCategories().clear(); 
        if (categoryIds != null) {
            categoryIds.forEach(id -> {
                categoryRepository.findById(id).ifPresent(category -> existingProduct.getCategories().add(category));
            });
        }
        
        productRepository.save(existingProduct); 
        redirectAttributes.addFlashAttribute("successMessage", "แก้ไขสินค้า '" + existingProduct.getName() + "' สำเร็จ!");
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteProduct(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            // (*** นี่คือจุดที่แก้ไข ***)
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found ID: " + id)); // (ต้องเป็นประโยคเดียวกัน)

            // (ตรวจสอบ Variants ก่อนลบ)
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "ลบสินค้า '" + product.getName() + "' ไม่ได้! ต้องลบ Variants ทั้งหมดก่อน");
                return "redirect:/products/view/" + id; 
            }
            
            product.getCategories().clear();
            productRepository.save(product);
            productRepository.delete(product); 
            redirectAttributes.addFlashAttribute("successMessage", "ลบสินค้า '" + product.getName() + "' สำเร็จ!");
        
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการลบ: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // =======================================================
    // <<< Product Variant Management >>>
    // =======================================================

    @GetMapping("/view/{id}")
    @Transactional(readOnly = true) 
    public String showProductDetail(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepository.findById(id); 

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Hibernate.initialize(product.getVariants()); 
            model.addAttribute("product", product);
            model.addAttribute("variants", product.getVariants()); 
            return "product-detail"; 
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบสินค้า ID: " + id);
            return "redirect:/products";
        }
    }

    @GetMapping("/variant/new")
    public String showCreateVariantForm(@RequestParam("productId") String productId, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Product หลัก");
            return "redirect:/products";
        }
        ProductVariant newVariant = new ProductVariant();
        newVariant.setProduct(productOpt.get()); 
        
        model.addAttribute("variant", newVariant);
        model.addAttribute("product", productOpt.get());
        model.addAttribute("isNew", true);
        return "product-variant-form"; 
    }

    @GetMapping("/variant/edit/{id}")
    public String showEditVariantForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ProductVariant> variantOpt = productVariantRepository.findById(id);
        if (!variantOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Variant ID: " + id);
            return "redirect:/products";
        }
        model.addAttribute("variant", variantOpt.get());
        model.addAttribute("product", variantOpt.get().getProduct()); 
        model.addAttribute("isNew", false);
        return "product-variant-form"; 
    }

    @PostMapping("/variant/save")
    public String saveProductVariant(
            @ModelAttribute ProductVariant variant, 
            @RequestParam("productId") String productId, 
            RedirectAttributes redirectAttributes) {
        
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Product หลักที่เชื่อมโยง");
            return "redirect:/products";
        }
        Product product = productOpt.get();
        variant.setProduct(product); 

        if (variant.getVariantID() == null || variant.getVariantID().isEmpty()) {
            variant.setVariantID(productService.generateNewVariantId(productId)); 
        }

        try {
            productVariantRepository.save(variant);
            redirectAttributes.addFlashAttribute("successMessage", "บันทึก Variant '" + variant.getVariantID() + "' สำเร็จ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "เกิดข้อผิดพลาดในการบันทึก Variant: " + e.getMessage());
        }
        return "redirect:/products/view/" + productId;
    }
    
    // =======================================================
    // <<< API Endpoints (สำหรับ JavaScript/Dropdown) >>>
    // =======================================================

    @GetMapping("/variants/{productId}") 
    @ResponseBody 
    @Transactional(readOnly = true) 
    public ResponseEntity<Set<ProductVariantDropdownDto>> getVariantsByProductId(@PathVariable String productId) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Hibernate.initialize(product.getVariants()); 
            
            Set<ProductVariantDropdownDto> variantsDto = product.getVariants().stream()
                .map(variant -> new ProductVariantDropdownDto(
                    variant.getVariantID(),      
                    variant.getSkuCode(),        
                    product.getName(),      
                    variant.getAttributes(),
                    variant.getUnitPrice()
                ))
                .collect(Collectors.toSet());

            return ResponseEntity.ok(variantsDto); 
        } else {
            return ResponseEntity.ok(Collections.emptySet()); 
        }
    }
}