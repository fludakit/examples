package com.example;

import io.github.fludakit.tx.jpa.PersistenceConfigurationCustomizer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceConfiguration;

@ApplicationScoped
public class EcommercePersistenceConfig implements PersistenceConfigurationCustomizer {

    @Override
    public String persistenceUnitName() {
        return "ecommerce";
    }

    @Override
    public void customize(PersistenceConfiguration configuration) {
        configuration.managedClass(Product.class)
                .managedClass(Order.class)
                .property("hibernate.hbm2ddl.auto", "none")
                .property("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    }
}
