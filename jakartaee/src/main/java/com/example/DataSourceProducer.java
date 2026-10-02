package com.example;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

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

    @Produces
    @ApplicationScoped
    public DataSource expose() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:app/MyDS");
    }
}
