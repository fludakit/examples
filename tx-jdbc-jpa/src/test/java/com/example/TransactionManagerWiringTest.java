package com.example;

import io.github.fludakit.tx.TransactionManager;
import io.github.fludakit.tx.jdbc.JdbcTransactionManager;
import io.github.fludakit.tx.jdbc.JdbcTransactionManagerProducer;
import io.github.fludakit.tx.jpa.JpaTransactionManager;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.util.TypeLiteral;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Boots the container from the classpath, the way {@link Main} does, so the deployment rules rather
 * than an explicit {@code WeldInitiator} producer list are what the assertions exercise.
 *
 * <p>No bean is instantiated: the point is what deployment registered, which is exactly what the
 * veto extension changes.</p>
 */
class TransactionManagerWiringTest {

    private static final TypeLiteral<TransactionManager<?>> TRANSACTION_MANAGER =
            new TypeLiteral<>() {
            };

    @Test
    void theVetoLeavesTheJpaManagerAsTheOnlyTransactionManager() {
        try (WeldContainer container = new Weld().initialize()) {
            // the exact type TransactionalInterceptor injects
            Set<Bean<?>> managers = container.getBeanManager().getBeans(TRANSACTION_MANAGER.getType());

            assertEquals(1, managers.size(),
                    () -> "expected the vetoed JDBC producer to leave one transaction manager, got " + managers);
            assertEquals(1, container.getBeanManager().getBeans(JpaTransactionManager.class).size(),
                    "the JPA manager must be the surviving one");
            assertEquals(0, container.getBeanManager().getBeans(JdbcTransactionManager.class).size(),
                    "the JDBC manager must not be registered");
            assertEquals(0, container.getBeanManager().getBeans(JdbcTransactionManagerProducer.class).size(),
                    "the JDBC producer must not be registered");
        }
    }
}
