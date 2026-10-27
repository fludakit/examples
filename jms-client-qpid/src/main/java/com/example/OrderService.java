package com.example;

import io.github.fludakit.jms.JmsClient;
import io.github.fludakit.jms.support.JmsMessages;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.time.Duration;

/**
 * The same CDI bean as in the Artemis example - the application code does not change when the
 * provider behind the client does, only the {@code fluda.jms.qpid.*} configuration does.
 *
 * <p>{@code client.destination()} uses the destination configured through
 * {@code fluda.jms.default-destination}. {@link Dependent} on purpose: a {@code JmsClient} is not
 * thread-safe, so it must not be shared by concurrent callers.</p>
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
