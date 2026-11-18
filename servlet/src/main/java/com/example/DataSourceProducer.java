package com.example;

import io.github.fludakit.tx.support.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * Exposes the container pool as the transaction-aware {@code DataSource}.
 *
 * <p>{@code JdbcTransactionManagerProducer} from {@code fluda-tx-jdbc} is the only
 * {@code TransactionManager} bean. The view is {@code @Singleton} so that producer sees the
 * {@code TransactionAwareDataSourceProxy} instance and reuses it.</p>
 */
@ApplicationScoped
public class DataSourceProducer {

    @Resource(name = "jdbc/myDS")
    private DataSource dataSource;

    @Produces
    @Singleton
    public DataSource expose() {
        return new TransactionAwareDataSourceProxy(dataSource);
    }
}
