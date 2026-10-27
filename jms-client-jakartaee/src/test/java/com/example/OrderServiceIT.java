package com.example;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Jakarta EE example inside a real container: the {@code @JMSDestinationDefinition} queue is
 * provisioned at deployment, the server's container-managed {@code JMSContext} is what
 * {@code fluda-jms-client-cdi} builds the client from, and the test gets that client's bean injected.
 *
 * <p>Run it against GlassFish ({@code -Parq-glassfish-managed}) or WildFly
 * ({@code -Parq-wildfly-managed}); both profiles unpack a server and start it through Arquillian.</p>
 */
@ArquillianTest
public class OrderServiceIT {

    @Inject
    OrderService service;

    @Deployment
    public static WebArchive createDeployment() {
        var libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("io.github.fludakit:fluda-jms-client-core",
                        "io.github.fludakit:fluda-jms-client-cdi")
                .withTransitivity().asFile();
        return ShrinkWrap.create(WebArchive.class, "jms-client-it.war")
                .addClasses(OrderService.class, Order.class, JmsResources.class)
                .addAsLibraries(libs)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    void sendsAndReceivesThroughTheInjectedQueue() {
        Order order = new Order("order-1", "keyboard", 2);

        service.place(order);

        assertEquals(order.text(), service.receive(Duration.ofSeconds(5)));
    }
}
