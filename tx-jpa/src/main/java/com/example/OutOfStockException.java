package com.example;

/**
 * Thrown when an order cannot be placed because the catalog has not enough stock left.
 */
public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String sku, int quantity, Integer available) {
        super("Out of stock: sku " + sku + " has " + available + " left, but " + quantity + " were ordered");
    }
}
