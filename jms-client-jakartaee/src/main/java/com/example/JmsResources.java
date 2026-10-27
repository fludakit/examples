package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.JMSDestinationDefinition;

/**
 * Declares the queue the example uses.
 *
 * <p>Jakarta EE does not let an application create destinations at runtime, so the queue is
 * declared with {@code @JMSDestinationDefinition} and the container provisions it at deployment:
 * {@code java:comp/jms/orders} is the JNDI name the application looks up, {@code orders} is the
 * physical destination name. The annotation is repeatable - a second queue would be another
 * annotation on the same class.</p>
 *
 * <p>This is the counterpart of the {@code fluda.jms.default-destination} property of the Java SE
 * examples: on a server the destination comes from the deployment, not from client configuration.</p>
 */
@JMSDestinationDefinition(
        name = "java:comp/jms/orders",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "orders"
)
@ApplicationScoped
public class JmsResources {
}
