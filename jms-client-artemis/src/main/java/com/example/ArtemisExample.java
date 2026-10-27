package com.example;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.time.Duration;

/**
 * Java SE + CDI usage of the JMS client against ActiveMQ Artemis.
 *
 * <p>Nothing here creates a connection factory: it is configured in
 * {@code META-INF/microprofile-config.properties} ({@code fluda.jms.artemis.*}) and the
 * {@code config} module builds it; the {@code cdi} module caches it and produces the
 * {@link io.github.fludakit.jms.JmsClient} this example injects.</p>
 *
 * <pre>{@code
 * docker run -d --name artemis -p 61616:61616 -e ARTEMIS_USER=artemis \
 *   -e ARTEMIS_PASSWORD=artemis apache/activemq-artemis:2.40.0
 * ./mvnw -pl jms-client-artemis exec:java -Dexec.mainClass=com.example.ArtemisExample
 * }</pre>
 *
 * <p>The defaults in {@code microprofile-config.properties} match that container; override them
 * with the {@code JMS_BROKER_URL}, {@code JMS_USERNAME} and {@code JMS_PASSWORD} environment
 * variables to point at another broker.</p>
 */
public final class ArtemisExample {

    private ArtemisExample() {
    }

    public static void main(String[] args) {
        try (WeldContainer container = new Weld().initialize()) {
            OrderService service = container.select(OrderService.class).get();

            Order order = new Order("order-1", "keyboard", 2);
            service.place(order);
            System.out.println("sent: " + order);

            Order received = service.receive(Duration.ofSeconds(5));
            System.out.println("received: " + received);
        }
    }
}
