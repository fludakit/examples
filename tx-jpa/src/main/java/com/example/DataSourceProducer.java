package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.fludakit.tx.RawDataSource;

import javax.sql.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DataSourceProducer {

    private final HikariDataSource raw;

    public DataSourceProducer() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:txjpa-ecommerce;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(10);
        this.raw = new HikariDataSource(config);
    }

    /**
     * The raw pool the CDI-produced {@code PersistenceConfiguration} binds the persistence unit
     * to; also the default pool application JDBC code draws from.
     */
    @Produces
    @ApplicationScoped
    @RawDataSource
    public DataSource rawDataSource() {
        return raw;
    }

    /**
     * The default unqualified pool, the same instance the persistence unit is bound to.
     */
    @Produces
    @ApplicationScoped
    public DataSource dataSource() {
        return raw;
    }
}
