package com.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private Integer stock;

    public Product() {
    }

    public Product(String sku, Integer stock) {
        this.sku = sku;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public Integer getStock() {
        return stock;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
