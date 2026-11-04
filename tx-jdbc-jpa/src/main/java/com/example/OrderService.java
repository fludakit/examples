package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_SHIPPED = "SHIPPED";

    @Inject
    JdbcClient client;

    @Inject
    EntityManager em;

    /**
     * Places an order in the same transaction: the catalog stock is decreased through the fluent
     * JDBC client while the order itself is persisted through the entity manager, so both share
     * one transaction.
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
        client.sql("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
                .param("productId", productId)
                .param("quantity", quantity)
                .update();

        Order order = new Order(productId, quantity, STATUS_CREATED);
        em.persist(order);
        return order;
    }

    @Transactional
    public void createProduct(String sku, int stock) {
        client.sql("INSERT INTO products (sku, stock) VALUES (:sku, :stock)")
                .param("sku", sku)
                .param("stock", stock)
                .update();
    }

    @Transactional
    public Order findOrder(long orderId) {
        return em.createQuery("SELECT o FROM Order o WHERE o.id = :id", Order.class)
                .setParameter("id", orderId)
                .getSingleResult();
    }

    @Transactional
    public Order cancelOrder(long orderId) {
        Order order = findOrder(orderId);
        client.sql("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
                .param("productId", order.getProductId())
                .param("quantity", order.getQuantity())
                .update();
        order.setStatus(STATUS_CANCELLED);
        return order;
    }

    @Transactional
    public void payOrder(long orderId) {
        Order order = findOrder(orderId);
        order.setStatus(STATUS_PAID);
    }

    @Transactional
    public void shipOrder(long orderId) {
        Order order = findOrder(orderId);
        order.setStatus(STATUS_SHIPPED);
    }

    @Transactional
    public int stock(String sku) {
        return client.sql("SELECT id, stock FROM products WHERE sku = :sku")
                .param("sku", sku)
                .query(Stock.class)
                .single()
                .stock();
    }
}
