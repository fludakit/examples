package com.example;

import javax.sql.DataSource;
import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * Declares a self-contained PostgreSQL {@link DataSource} under the portable {@code java:app/MyDS}
 * JNDI name and exposes it as a CDI bean.
 */
@DataSourceDefinition(
        name = "java:app/MyDS",
        className = "org.postgresql.xa.PGXADataSource", // org.postgresql.xa.PGXADataSource for JTA multiple ds
        url = "jdbc:postgresql://localhost:5432/testdb",
        user = "testuser",
        password = "testpassword"
)
@ApplicationScoped
public class DataSourceProducer {

    @Resource(lookup = "java:app/MyDS")
    private DataSource dataSource;

    @Produces
    @ApplicationScoped
    public DataSource expose() {
        return dataSource;
    }
}
