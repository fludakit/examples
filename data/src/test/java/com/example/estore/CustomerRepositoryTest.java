package com.example.estore;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.JdbcClient;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(WeldJunit5Extension.class)
class CustomerRepositoryTest {

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

    @Test
    void crudAndAnnotationDrivenFinders() {
        Customer ana = customers.insert(customer("Ana", "ana@example.com"));
        assertThat(ana.getId()).isNotNull();
        assertThat(customers.findById(ana.getId())).isPresent();
        assertThat(customers.findByName("Ana")).hasSize(1);
        assertThat(customers.findByEmail("ana@example.com")).hasSize(1);

        products.insert(product("DDD", new BigDecimal("39.99")));
        assertThat(products.findByName("DDD")).hasSize(1);
        assertThat(products.findByPrice(new BigDecimal("39.99")))
                .extracting(Product::getName).containsExactly("DDD");
        assertThat(products.findByPrice(new BigDecimal("1.00"))).isEmpty();
    }

    @Test
    void rereadPopulatesAllScalarColumnsViaGeneratedMapper() {
        Customer ana = customers.insert(customer("Ana", "ana@example.com"));
        Product book = products.insert(product("DDD", new BigDecimal("39.99")));

        Customer reloadedCustomer = customers.findById(ana.getId()).orElseThrow();
        assertThat(reloadedCustomer.getName()).isEqualTo("Ana");
        assertThat(reloadedCustomer.getEmail()).isEqualTo("ana@example.com");

        Product reloadedProduct = products.findById(book.getId()).orElseThrow();
        assertThat(reloadedProduct.getName()).isEqualTo("DDD");
        assertThat(reloadedProduct.getPrice()).isEqualByComparingTo("39.99");
    }

    @Test
    void findAllPageHonoursOrder() {
        customers.insert(customer("Charlie", "charlie@example.com"));
        customers.insert(customer("Alice", "alice@example.com"));
        customers.insert(customer("Bob", "bob@example.com"));

        Page<Customer> page = customers.findAll(
                PageRequest.ofSize(2), jakarta.data.Order.by(_Customer.name.asc()));

        assertThat(page.numberOfElements()).isEqualTo(2);
        assertThat(page.content()).extracting(Customer::getName).containsExactly("Alice", "Bob");
    }

    @Test
    void savesEntityGraphWithRelationships() {
        Customer ana = customers.save(customer("Ana", "ana@example.com"));
        Product book = products.save(product("DDD", new BigDecimal("39.99")));
        Order order = new Order(ana);
        order.addItem(book, 2);
        order.setAmount(new BigDecimal("79.98"));
        Order saved = orders.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomer().getId()).isEqualTo(ana.getId());
        assertThat(saved.getOrderItems()).hasSize(1);
        assertThat(saved.getOrderItems().get(0).getProduct().getId()).isEqualTo(book.getId());
    }

    @Test
    void cursoredPagePagesById() {
        customers.insert(customer("Charlie", "charlie@example.com"));
        customers.insert(customer("Alice", "alice@example.com"));
        customers.insert(customer("Bob", "bob@example.com"));

        CursoredPage<Customer> first = customers.paged(PageRequest.ofSize(2));
        Assertions.assertThat(first).isInstanceOf(CursoredPage.class);
        assertThat(first.content()).hasSize(2);
        if (first.hasNext()) {
            CursoredPage<Customer> second = (CursoredPage<Customer>) customers.paged(first.nextPageRequest());
            assertThat(second.content()).isNotEmpty();
        }
    }

    @Test
    void exposesTheInjectedJdbcClientThroughADeclaredAccessor() {
        // CustomerRepository declares `JdbcClient getJdbcClient();` like any other method, and the
        // generated CustomerRepositoryImpl answers it with the client its own generated methods run
        // through. An ad-hoc statement proves it is the SAME connection, not a second one.
        customers.insert(customer("Ana", "ana@example.com"));
        customers.insert(customer("Bo", "bo@example.com"));

        JdbcClient client = customers.getJdbcClient();
        assertThat(client).isNotNull();
        assertThat(client.sql("SELECT COUNT(*) FROM \"customer\"").singleValue(Long.class)).isEqualTo(2L);
        assertThat(client.sql("SELECT name FROM \"customer\" ORDER BY name").query(String.class).list())
                .containsExactly("Ana", "Bo");
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
