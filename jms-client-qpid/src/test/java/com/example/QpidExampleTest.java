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
 * The Qpid CDI example against the AMQP acceptor of a Testcontainers Artemis broker (Docker
 * required).
 *
 * <p>Artemis exposes the AMQP 1.0 acceptor on port 5672 next to the core protocol one on 61616, so
 * the same container serves both examples. The system properties are set before the Weld container
 * starts, because they override the placeholders of {@code microprofile-config.properties}.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
class QpidExampleTest {

    @Container
    static final ArtemisContainer artemis = new ArtemisContainer("apache/activemq-artemis:2.40.0");

    /** The broker exposes a dedicated AMQP-only acceptor next to the CORE protocol one. */
    private static final int AMQP_PORT = 5672;

    private static WeldContainer container;

    @BeforeAll
    static void setUp() {
        System.setProperty("fluda.jms.qpid.remote-url",
                "amqp://" + artemis.getHost() + ":" + artemis.getMappedPort(AMQP_PORT));
        System.setProperty("fluda.jms.qpid.username", artemis.getUser());
        System.setProperty("fluda.jms.qpid.password", artemis.getPassword());
        container = new Weld().initialize();
    }

    @AfterAll
    static void tearDown() {
        try {
            if (container != null) {
                container.close();
            }
        } finally {
            System.clearProperty("fluda.jms.qpid.remote-url");
            System.clearProperty("fluda.jms.qpid.username");
            System.clearProperty("fluda.jms.qpid.password");
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
