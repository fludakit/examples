package com.example;

/**
 * A row of the {@code products} catalog table: the product id and its remaining stock.
 */
public record Stock(Long id, Integer stock) {
}
