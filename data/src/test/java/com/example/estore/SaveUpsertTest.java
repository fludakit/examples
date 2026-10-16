package com.example.estore;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Jakarta Data defines {@code save} as an <strong>upsert</strong>: saving an entity that carries an
 * application-assigned id inserts when no row exists and updates when it does; a null id lets the
 * database assign the key.
 */
@ExtendWith(WeldJunit5Extension.class)
class SaveUpsertTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            ProductRepositoryImpl.class).build();

    @Inject ProductRepository products;

    @Test
    void saveWithAnAssignedIdInsertsWhenNoRowExists() {
        products.save(product(9001L, "Pen", "2.50"));

        assertThat(products.findById(9001L)).isPresent();
        assertThat(products.findById(9001L).orElseThrow().getName()).isEqualTo("Pen");
    }

    @Test
    void saveWithAnAssignedIdUpdatesWhenTheRowExists() {
        Product marker = product(9002L, "Before", "1.00");
        products.save(marker);

        marker.setName("After");
        marker.setPrice(new BigDecimal("3.00"));
        products.save(marker);

        assertThat(products.findById(9002L).orElseThrow().getName()).isEqualTo("After");
        assertThat(products.findByName("Before")).isEmpty();
    }

    @Test
    void saveWithANullIdStillLetsTheDatabaseAssignTheKey() {
        Product widget = product(null, "Widget", "5.00");

        products.save(widget);

        assertThat(widget.getId()).isNotNull();
        assertThat(products.findById(widget.getId())).isPresent();
    }

    private static Product product(Long id, String name, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }
}
