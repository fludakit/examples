package com.example;

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
    EntityManager em;

    /**
     * Places an order: the catalog stock is decreased in the same transaction that inserts the
     * order, so a failure in between leaves both consistent.
     */
    @Transactional
    public Order placeOrder(String sku, int quantity) {
        Product product = em.createQuery("SELECT p FROM Product p WHERE p.sku = :sku", Product.class)
                .setParameter("sku", sku)
                .getSingleResult();

        if (product.getStock() == null || product.getStock() < quantity) {
            throw new OutOfStockException(sku, quantity, product.getStock());
        }

        Order order = new Order(product.getId(), quantity, STATUS_CREATED);
        em.persist(order);
        product.setStock(product.getStock() - quantity);
        return order;
    }

    @Transactional
    public Order findOrder(long orderId) {
        return em.createQuery("SELECT o FROM Order o WHERE o.id = :id", Order.class)
                .setParameter("id", orderId)
                .getSingleResult();
    }

    @Transactional
    public void createProduct(String sku, int stock) {
        em.persist(new Product(sku, stock));
    }

    @Transactional
    public Order cancelOrder(long orderId) {
        Order order = findOrder(orderId);
        Product product = em.find(Product.class, order.getProductId());
        product.setStock(product.getStock() + order.getQuantity());
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
        return em.createQuery("SELECT p FROM Product p WHERE p.sku = :sku", Product.class)
                .setParameter("sku", sku)
                .getSingleResult()
                .getStock();
    }
}
