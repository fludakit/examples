package com.example.estore;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Every finder here is annotation-driven. Operator predicates — price ranges, name patterns — are a
 * query-by-method-name feature and deliberately do NOT appear in this example (see
 * <a href="../../data-qbn">data-qbn</a> for those).
 */
@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {

    @Find
    List<Product> findByName(@By("name") String name);

    @Find
    List<Product> findByPrice(@By("price") BigDecimal price);
}
