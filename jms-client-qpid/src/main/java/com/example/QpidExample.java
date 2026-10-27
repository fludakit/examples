package com.example;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.time.Duration;

/**
 * Java SE + CDI usage of the JMS client over AMQP 1.0, with Apache Qpid JMS as the client library.
 *
 * <p>Compared with the Artemis example, only {@code META-INF/microprofile-config.properties}
 * differs: the {@code fluda.jms.qpid.*} section replaces {@code fluda.jms.artemis.*}, and the
 * {@code config} module builds a Qpid connection factory when the Qpid client is on the
 * classpath.</p>
 *
 * <pre>{@code
 * docker run -d --name artemis -p 61616:61616 -p 5672:5672 -e ARTEMIS_USER=artemis \
 *   -e ARTEMIS_PASSWORD=artemis apache/activemq-artemis:2.40.0
 * ./mvnw -pl jms-client-qpid exec:java -Dexec.mainClass=com.example.QpidExample
 * }</pre>
 *
 * <p>The Artemis container above speaks AMQP on 5672; override the URL, user and password with
 * {@code JMS_BROKER_URL}, {@code JMS_USERNAME} and {@code JMS_PASSWORD} to use another broker.</p>
 */
public final class QpidExample {

    private QpidExample() {
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
