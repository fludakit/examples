package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import io.github.fludakit.jdbc.support.GeneratedKeyHolder;
import io.github.fludakit.jdbc.support.KeyHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_SHIPPED = "SHIPPED";

    @Inject
    JdbcClient client;

    /**
     * Places an order: the catalog stock is decreased in the same transaction that inserts the
     * order, so a failure in between leaves both consistent.
     */
    @Transactional
    public Order placeOrder(String sku, int quantity) {
        Stock stock = client.sql("SELECT id, stock FROM products WHERE sku = :sku")
                .param("sku", sku)
                .query(Stock.class)
                .single();

        if (stock.stock() == null || stock.stock() < quantity) {
            throw new OutOfStockException(sku, quantity, stock.stock());
        }

        Long productId = stock.id();
        KeyHolder holder = new GeneratedKeyHolder();
        client.sql("INSERT INTO orders (product_id, quantity, status) VALUES (:productId, :quantity, :status)")
                .param("productId", productId)
                .param("quantity", quantity)
                .param("status", STATUS_CREATED)
                .update(holder);
        long orderId = ((Number) holder.getKey()).longValue();

        client.sql("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
                .param("productId", productId)
                .param("quantity", quantity)
                .update();

        return new Order(orderId, productId, quantity, STATUS_CREATED);
    }

    @Transactional
    public Order findOrder(long orderId) {
        return client.sql("SELECT id, product_id, quantity, status FROM orders WHERE id = :orderId")
                .param("orderId", orderId)
                .query(Order.class)
                .single();
    }

    @Transactional
    public Order cancelOrder(long orderId) {
        Order order = findOrder(orderId);
        client.sql("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
                .param("productId", order.productId())
                .param("quantity", order.quantity())
                .update();
        client.sql("UPDATE orders SET status = :status WHERE id = :orderId")
                .param("orderId", orderId)
                .param("status", STATUS_CANCELLED)
                .update();
        return new Order(order.id(), order.productId(), order.quantity(), STATUS_CANCELLED);
    }

    @Transactional
    public void payOrder(long orderId) {
        client.sql("UPDATE orders SET status = :status WHERE id = :orderId")
                .param("orderId", orderId)
                .param("status", STATUS_PAID)
                .update();
    }

    @Transactional
    public void shipOrder(long orderId) {
        client.sql("UPDATE orders SET status = :status WHERE id = :orderId")
                .param("orderId", orderId)
                .param("status", STATUS_SHIPPED)
                .update();
    }
}
