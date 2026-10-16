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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the read semantics of the {@code @Association} header target {@code Order.customer}:
 *
 * <ul>
 *   <li>a plain entity read (findById) materialises the association target as an id-only partial;</li>
 *   <li>a finder whose predicate traverses the association ({@code findByCustomerName}) is the
 *       join-aware read and returns the traversed association FULLY populated.</li>
 * </ul>
 */
@ExtendWith(WeldJunit5Extension.class)
class OrderAssociationTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            CustomerRepositoryImpl.class,
            OrderRepositoryImpl.class).build();

    @Inject CustomerRepository customers;
    @Inject OrderRepository orders;

    @Test
    void findByIdMaterialisesAssociationAsIdOnlyPartial() {
        Customer ana = customers.save(customer("Ana", "ana@example.com"));
        Order order = new Order(ana);
        order.setAmount(new BigDecimal("10.00"));
        Order saved = orders.save(order);

        Order reloaded = orders.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getCustomer()).isNotNull();
        assertThat(reloaded.getCustomer().getId()).isEqualTo(ana.getId());
        assertThat(reloaded.getCustomer().getName()).isNull(); // association materialises id only
    }

    @Test
    void finderThroughAssociationMaterialisesTargetFully() {
        Customer ana = customers.save(customer("Ana", "ana@example.com"));
        Customer bob = customers.save(customer("Bob", "bob@example.com"));
        Order anaOrder = new Order(ana);
        anaOrder.setAmount(new BigDecimal("20.00"));
        orders.save(anaOrder);
        Order bobOrder = new Order(bob);
        bobOrder.setAmount(new BigDecimal("30.00"));
        orders.save(bobOrder);

        List<Order> found = orders.findByCustomerName("Ana");
        assertThat(found).hasSize(1);
        Order order = found.get(0);
        // Joined read: the traversed association is fully materialised, not an id-only partial.
        assertThat(order.getCustomer().getName()).isEqualTo("Ana");
        assertThat(order.getCustomer().getEmail()).isEqualTo("ana@example.com");
        assertThat(order.getAmount()).isEqualByComparingTo("20.00");
    }

    private static Customer customer(String name, String email) {
        Customer c = new Customer();
        c.setName(name);
        c.setEmail(email);
        return c;
    }
}
