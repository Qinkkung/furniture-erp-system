package com.furnitureshop.furniture_erp_system.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "category_id", length = 10)
    private String categoryID;

    public String getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<Product> getProducts() {
		return products;
	}

	public void setProducts(Set<Product> products) {
		this.products = products;
	}

	@Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", length = 50)
    private String type;

    // --- Relationship ---
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY) // อ้างอิงกลับไปที่ Product
    private Set<Product> products = new HashSet<>();

    // --- Constructor ---
    public Category() {
    }


}