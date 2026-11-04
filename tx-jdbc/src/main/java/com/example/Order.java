package com.example;

/**
 * A row of the {@code orders} table: which product was bought, how many, and the lifecycle status.
 */
public record Order(Long id, Long productId, Integer quantity, String status) {
}
