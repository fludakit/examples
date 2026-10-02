package com.example;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import jakarta.inject.Inject;

@ArquillianTest
public class DataSourceInjectionIT {
    private static final Logger LOGGER = Logger.getLogger(DataSourceInjectionIT.class.getName());

    @Inject
    private DataSource dataSource;

    @Deployment
    public static WebArchive createDeployment() {
        var libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.postgresql:postgresql"
                )
                .withoutTransitivity()
                .asFile();
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test-datasource.war")
                .addAsLibraries(libs)
                .addClasses(DataSourceProducer.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "deployment archive: {0}", archive.toString(true));
        return archive;
    }

    @Test
    public void dataSourceInjectionSelectOne() throws Exception {
        Assertions.assertNotNull(dataSource, "DataSource should be injected");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            Assertions.assertTrue(rs.next());
            Assertions.assertEquals(1, rs.getInt(1));
        }
    }
}
