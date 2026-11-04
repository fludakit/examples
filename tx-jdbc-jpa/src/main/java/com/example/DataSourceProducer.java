package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.fludakit.tx.RawDataSource;
import io.github.fludakit.tx.jdbc.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DataSourceProducer {

    private final HikariDataSource raw;

    public DataSourceProducer() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:txjdbjpaecommerce;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        this.raw = new HikariDataSource(config);
    }

    /**
     * The raw pool the CDI-produced {@code PersistenceConfiguration} binds the persistence unit
     * to.
     */
    @Produces
    @ApplicationScoped
    @RawDataSource
    public DataSource rawDataSource() {
        return raw;
    }

    /**
     * The default DataSource: the transaction-aware view the injected {@code JdbcClient} draws
     * from, and the raw pool the JPA persistence unit is built on.
     */
    @Produces
    @ApplicationScoped
    public DataSource dataSource() {
        return new TransactionAwareDataSourceProxy(raw);
    }
}
