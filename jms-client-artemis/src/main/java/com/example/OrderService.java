package com.example;

import io.github.fludakit.jms.JmsClient;
import io.github.fludakit.jms.support.JmsMessages;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.time.Duration;

/**
 * A CDI bean around an injected {@link JmsClient}.
 *
 * <p>It never names a destination: {@code client.destination()} uses the one configured through
 * {@code fluda.jms.default-destination} in {@code microprofile-config.properties}. The client
 * itself, its connection factory and its caching all come from the {@code cdi} and {@code config}
 * modules - this class only injects.</p>
 *
 * <p>{@link Dependent} on purpose: a {@code JmsClient} is not thread-safe, so it must not be shared
 * by concurrent callers. Inject this service per operation, or into a request-scoped bean.</p>
 */
@Dependent
public class OrderService {

    @Inject
    JmsClient client;

    /** Sends the order and tags the message with its id. */
    public void place(Order order) {
        client.destination()
                .send(order, message -> JmsMessages.setStringProperty(message, "orderId", order.id()));
    }

    /** Blocks until an order arrives or the timeout expires. */
    public Order receive(Duration timeout) {
        return client.destination().receive(Order.class, timeout);
    }
}
