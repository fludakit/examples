package com.example;

import io.github.fludakit.jms.CachingConnectionFactory;
import io.github.fludakit.jms.JmsClient;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

import java.time.Duration;

/**
 * Plain Java SE usage of the JMS client: build a client from the provider's factory, send a
 * serializable payload and receive it back.
 *
 * <p>No CDI, no MicroProfile Config. The broker location and credentials come from environment
 * variables so the example can point at any Artemis instance:</p>
 *
 * <pre>{@code
 * JMS_BROKER_URL=tcp://localhost:61616 JMS_USERNAME=artemis JMS_PASSWORD=artemis \
 *   ./mvnw -pl jms-client-vanilla exec:java -Dexec.mainClass=com.example.VanillaExample
 * }</pre>
 *
 * <p>The defaults match the official Artemis container used by the test suite.</p>
 */
public final class VanillaExample {

    private static final String DEFAULT_BROKER_URL = "tcp://localhost:61616";
    private static final String DEFAULT_USERNAME = "artemis";
    private static final String DEFAULT_PASSWORD = "artemis";

    private VanillaExample() {
    }

    public static void main(String[] args) {
        String brokerUrl = setting("JMS_BROKER_URL", DEFAULT_BROKER_URL);
        String username = setting("JMS_USERNAME", DEFAULT_USERNAME);
        String password = setting("JMS_PASSWORD", DEFAULT_PASSWORD);

        try (var factory = new ActiveMQJMSConnectionFactory(brokerUrl, username, password);
             var caching = new CachingConnectionFactory(factory)) {
            OrderService service = new OrderService(JmsClient.builder(caching).build());

            Order order = new Order("order-1", "keyboard", 2);
            service.place(order);
            System.out.println("sent: " + order);

            Order received = service.receive(Duration.ofSeconds(5));
            System.out.println("received: " + received);
        }
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
