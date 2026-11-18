package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fludakit.tx.support.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * The pool and the transaction-aware view.
 *
 * <p>{@code JdbcTransactionManagerProducer} from {@code fluda-tx-jdbc} supplies the
 * {@code TransactionManager}. The view is {@code @Singleton} so that producer sees the
 * {@code TransactionAwareDataSourceProxy} instance and reuses it.</p>
 */
@ApplicationScoped
public class DataSourceProducer {

    private final HikariDataSource raw;

    public DataSourceProducer() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:javase;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        this.raw = new HikariDataSource(config);
    }

    @Produces
    @Singleton
    public DataSource dataSource() {
        return new TransactionAwareDataSourceProxy(raw);
    }
}
