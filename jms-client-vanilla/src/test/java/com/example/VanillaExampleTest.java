package com.example;

import io.github.fludakit.jms.CachingConnectionFactory;
import io.github.fludakit.jms.JmsClient;
import io.github.fludakit.jms.support.JmsMessages;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The vanilla example against a real Artemis broker from Testcontainers (Docker required).
 */
@Testcontainers(disabledWithoutDocker = true)
class VanillaExampleTest {

    @Container
    static final ArtemisContainer artemis = new ArtemisContainer("apache/activemq-artemis:2.40.0");

    private ActiveMQJMSConnectionFactory factory;
    private CachingConnectionFactory caching;
    private JmsClient client;

    @BeforeEach
    void setUp() {
        factory = new ActiveMQJMSConnectionFactory(
                artemis.getBrokerUrl(), artemis.getUser(), artemis.getPassword());
        caching = new CachingConnectionFactory(factory);
        client = JmsClient.builder(caching).build();
    }

    @AfterEach
    void tearDown() {
        caching.close();
        factory.close();
    }

    @Test
    void sendsAndReceivesAnOrder() {
        OrderService service = new OrderService(client);
        Order order = new Order("order-1", "keyboard", 2);

        service.place(order);

        assertEquals(order, service.receive(Duration.ofSeconds(5)));
    }

    @Test
    void sendsATextMessageWithProperties() {
        client.destination("audit")
                .send("order placed", message -> JmsMessages.setStringProperty(message, "source", "vanilla"));

        List<String> messages = client.destination("audit").browse(JmsMessages::textOf);

        assertEquals(List.of("order placed"), messages);
    }
}
