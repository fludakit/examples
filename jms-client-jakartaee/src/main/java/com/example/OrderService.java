package com.example;

import io.github.fludakit.jms.JmsClient;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.jms.Queue;

import java.time.Duration;

/**
 * A CDI bean around the injected {@link JmsClient}.
 *
 * <p>Nothing here creates a client, a context or a connection. The Jakarta Messaging specification
 * makes a container-managed {@code JMSContext} injectable, and the server supplies one from
 * {@code java:comp/DefaultJMSConnectionFactory}; {@code fluda-jms-client-cdi} takes that context
 * first, before any connection factory or configured provider section. Credentials and session mode
 * therefore belong to the container, not to this application.</p>
 *
 * <p>The destination is the injected {@link Queue} - the resource declared by
 * {@link JmsResources} - not a name: on Jakarta EE the deployment decides the address, the queue
 * kind and the provider settings, so the application passes the injected {@code Destination} rather
 * than letting the client resolve a name.</p>
 *
 * <p>There is deliberately no {@code JMSContext} producer here: the server already exposes one as a
 * bean, and a second one would make an {@code Instance<JMSContext>} lookup ambiguous.</p>
 */
@Dependent
public class OrderService {

    @Inject
    JmsClient client;

    @Resource(lookup = "java:comp/jms/orders")
    Queue orders;

    /** Sends the order as a text message. */
    public void place(Order order) {
        client.destination(orders).send(order.text());
    }

    /** Blocks until an order arrives or the timeout expires. */
    public String receive(Duration timeout) {
        return client.destination(orders).receive(String.class, timeout);
    }
}
