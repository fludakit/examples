package com.example.estore;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.JdbcClient;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trips a {@code List<@ValueObject>} element collection through the generated child-table
 * hydration: reads materialise parents then group children onto each one; update replaces the whole
 * child set; delete cascades the children.
 */
@ExtendWith(WeldJunit5Extension.class)
class OrderCollectionHydrationTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            CustomerRepositoryImpl.class,
            ProductRepositoryImpl.class,
            OrderRepositoryImpl.class).build();

    @Inject CustomerRepository customers;
    @Inject ProductRepository products;
    @Inject OrderRepository orders;
    @Inject JdbcClient jdbcClient;

    @Test
    void collectionRoundTrip() {
        Customer ana = customers.save(customer("Ana", "ana@example.com"));
        Product widget = products.save(product("Widget", new BigDecimal("1.00")));
        Product gizmo = products.save(product("Gizmo", new BigDecimal("2.00")));

        Order order = new Order(ana);
        order.addItem(widget, 2);
        order.addItem(gizmo, 5);
        Order saved = orders.save(order);

        Order found = orders.findById(saved.getId()).orElseThrow();
        assertThat(found.getOrderItems()).hasSize(2);
        assertThat(found.getOrderItems()).extracting(OrderItem::getQuantity).containsExactlyInAnyOrder(2, 5);
        // Children rebuilt by the element-collection loader leave their @Association target null.
        assertThat(found.getOrderItems()).allSatisfy(i -> assertThat(i.getProduct()).isNull());
        assertThat(found.getCustomer().getId()).isEqualTo(ana.getId());

        // replace-all update: drop the two children, add a single one back.
        found.getOrderItems().clear();
        found.addItem(widget, 7);
        orders.update(found);

        Order afterUpdate = orders.findById(saved.getId()).orElseThrow();
        assertThat(afterUpdate.getOrderItems()).hasSize(1);
        assertThat(afterUpdate.getOrderItems().get(0).getQuantity()).isEqualTo(7);

        // delete cascades children: the header is gone and the child table no longer references it.
        orders.delete(found);
        Long remaining = jdbcClient.sql("SELECT COUNT(*) FROM order_order_items WHERE order_id = :id")
                .param("id", saved.getId()).singleValue(Long.class);
        assertThat(remaining).isZero();
    }

    @Test
    void findByAmountHydratesOrderItems() {
        Customer ana = customers.save(customer("Ana", "ana@example.com"));
        Product widget = products.save(product("Widget", new BigDecimal("1.00")));
        Order order = new Order(ana);
        order.setAmount(new BigDecimal("42.00"));
        order.addItem(widget, 3);
        orders.save(order);

        List<Order> found = orders.findByAmount(new BigDecimal("42.00"));
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getOrderItems()).hasSize(1);
        assertThat(found.get(0).getOrderItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(found.get(0).getOrderItems().get(0).getProduct()).isNull();
    }

    private static Customer customer(String name, String email) {
        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        return c;
    }

    private static Product product(String name, BigDecimal price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        return p;
    }
}
