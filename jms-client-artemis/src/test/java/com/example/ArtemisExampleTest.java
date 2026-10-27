package com.example;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Artemis CDI example against a real broker from Testcontainers (Docker required).
 *
 * <p>The system properties are set before the Weld container starts - they override the
 * placeholders of {@code microprofile-config.properties}, which point at {@code localhost:61616}.
 * A test property source cannot be used here: the container starts in the {@code @BeforeAll}
 * callback of the Testcontainers extension, before any method of this class runs.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
class ArtemisExampleTest {

    @Container
    static final ArtemisContainer artemis = new ArtemisContainer("apache/activemq-artemis:2.40.0");

    private static WeldContainer container;

    @BeforeAll
    static void setUp() {
        System.setProperty("fluda.jms.artemis.broker-url", artemis.getBrokerUrl());
        System.setProperty("fluda.jms.artemis.username", artemis.getUser());
        System.setProperty("fluda.jms.artemis.password", artemis.getPassword());
        container = new Weld().initialize();
    }

    @AfterAll
    static void tearDown() {
        try {
            if (container != null) {
                container.close();
            }
        } finally {
            System.clearProperty("fluda.jms.artemis.broker-url");
            System.clearProperty("fluda.jms.artemis.username");
            System.clearProperty("fluda.jms.artemis.password");
        }
    }

    @Test
    void sendsAndReceivesAnOrderThroughTheDefaultDestination() {
        OrderService service = container.select(OrderService.class).get();
        Order order = new Order("order-1", "keyboard", 2);

        service.place(order);

        assertEquals(order, service.receive(Duration.ofSeconds(5)));
    }
}
