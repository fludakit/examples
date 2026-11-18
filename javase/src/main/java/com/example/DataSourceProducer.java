package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fludakit.tx.TransactionManager;
import io.github.fludakit.tx.jdbc.JdbcTransactionManager;
import io.github.fludakit.tx.support.TransactionAwareDataSourceProxy;

import java.sql.Connection;
import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

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
    @ApplicationScoped
    public DataSource dataSource() {
        return new TransactionAwareDataSourceProxy(raw);
    }

    @Produces
    @ApplicationScoped
    public TransactionManager<Connection> transactionManager() {
        return new JdbcTransactionManager(raw);
    }
}
