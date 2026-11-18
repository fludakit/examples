package com.example;

import io.github.fludakit.tx.TransactionManager;
import io.github.fludakit.tx.jdbc.JdbcTransactionManager;
import io.github.fludakit.tx.support.TransactionAwareDataSourceProxy;

import java.sql.Connection;
import javax.sql.DataSource;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DataSourceProducer {

    @Resource(name = "jdbc/myDS")
    private DataSource dataSource;

    @Produces
    @ApplicationScoped
    public DataSource expose() {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Produces
    @ApplicationScoped
    public TransactionManager<Connection> transactionManager() {
        return new JdbcTransactionManager(dataSource);
    }
}
