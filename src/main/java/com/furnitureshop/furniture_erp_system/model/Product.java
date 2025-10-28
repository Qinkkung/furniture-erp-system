package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*; // Import ทั้งหมดจาก jakarta.persistence
import java.util.Set;
import java.util.HashSet; // Import HashSet สำหรับ Initializing Set
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "product_id", length = 10) // กำหนด length สำหรับ VARCHAR
    private String productID;

    public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public Set<ProductVariant> getVariants() {
		return variants;
	}

	public void setVariants(Set<ProductVariant> variants) {
		this.variants = variants;
	}

	@Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // --- Relationships ---
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE }) // ปรับ Cascade Type
    @JoinTable(
        name = "product_categories",
        joinColumns = { @JoinColumn(name = "product_id") },
        inverseJoinColumns = { @JoinColumn(name = "category_id") }
    )
    private Set<Category> categories = new HashSet<>(); // Initialize Set

    @JsonManagedReference
    @OneToMany(
        mappedBy = "product",
        cascade = CascadeType.ALL, // ลบ Product ให้ลบ Variant ด้วย
        fetch = FetchType.LAZY,
        orphanRemoval = true // ลบ Variant ที่ไม่ผูกกับ Product แล้ว
    )
    
    private Set<ProductVariant> variants = new HashSet<>(); // Initialize Set

    // --- Constructors (ควรมี constructor ว่าง) ---
    public Product() {
    }


}