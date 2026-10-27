package com.example;

import io.github.fludakit.jms.JmsClient;
import io.github.fludakit.jms.support.JmsMessages;

import java.time.Duration;

/**
 * Plain Java SE service around a {@link JmsClient} - no CDI, no configuration module. The client
 * is supplied by the caller, which owns the factory and the JMS context.
 */
public class OrderService {

    static final String QUEUE = "orders";

    private final JmsClient client;

    public OrderService(JmsClient client) {
        this.client = client;
    }

    /** Sends the order and tags the message with its id. */
    public void place(Order order) {
        client.destination(QUEUE)
                .send(order, message -> JmsMessages.setStringProperty(message, "orderId", order.id()));
    }

    /** Blocks until an order arrives or the timeout expires. */
    public Order receive(Duration timeout) {
        return client.destination(QUEUE).receive(Order.class, timeout);
    }
}
