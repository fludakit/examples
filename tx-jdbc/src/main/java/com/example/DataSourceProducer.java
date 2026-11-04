package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.fludakit.tx.jdbc.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DataSourceProducer {

    private final TransactionAwareDataSourceProxy dataSource;

    public DataSourceProducer() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:txjdbc-ecommerce;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        this.dataSource = new TransactionAwareDataSourceProxy(new HikariDataSource(config));
    }

    /**
     * The one {@code DataSource} the application has: the transaction-aware view over the pool.
     * Application JDBC code — including the injected {@code JdbcClient} — draws from it, and the
     * {@code JdbcTransactionManager} reuses it instead of creating a second proxy. Inside a
     * transaction it hands out the bound connection and suppresses close.
     */
    @Produces
    @ApplicationScoped
    public DataSource dataSource() {
        return dataSource;
    }
}
