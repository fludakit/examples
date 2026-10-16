package com.example.estore;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

    @Find
    List<Order> findByAmount(@By("amount") BigDecimal amount);

    /**
     * A predicate THROUGH the {@code @Association} reference: {@code @By} declares the traversal and
     * the resolver descends the relation head, contributing one join, so the returned orders carry a
     * fully populated customer rather than an id-only partial.
     */
    @Find
    List<Order> findByCustomerName(@By("customer.name") String name);
}
