package com.example;

import io.github.fludakit.tx.jdbc.JdbcTransactionManagerProducer;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Vetoes the JDBC transaction manager producer that {@code fluda-tx-jdbc} registers.
 *
 * <p>Both {@code fluda-tx-jdbc} and {@code fluda-tx-jpa} are on the classpath, so both transaction
 * manager producers are discovered and an injection point of {@code TransactionManager<?>} is
 * ambiguous. This application runs every step in a JPA-managed transaction — the
 * {@code JdbcClient} joins it through the {@code TransactionAwareDataSourceProxy} — so the JDBC
 * manager is vetoed and the JPA manager stays the only {@code TransactionManager} bean.</p>
 *
 * <p>CDI's {@code <scan>/<exclude>} only narrows the archive that declares it, so the library's
 * bean archive cannot be filtered from the application's {@code beans.xml}. Weld SE also scans the
 * classpath as one flat deployment, so this extension is registered through
 * {@code META-INF/services/jakarta.enterprise.inject.spi.Extension} rather than through
 * {@code beans.xml}.</p>
 */
public class DisableJdbcTransactionManager implements Extension {

    public void vetoJdbcTransactionManager(@Observes ProcessAnnotatedType<?> event) {
        if (event.getAnnotatedType().getJavaClass().equals(JdbcTransactionManagerProducer.class)) {
            event.veto();
        }
    }
}
