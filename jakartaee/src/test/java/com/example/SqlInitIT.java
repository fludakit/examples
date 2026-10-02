package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ArquillianTest
public class SqlInitIT {
    private static final Logger LOGGER = Logger.getLogger(SqlInitIT.class.getName());

    @Inject
    private JdbcClient jdbcClient;

    @Deployment
    public static WebArchive createDeployment() {
        var libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.postgresql:postgresql",
                        "io.github.fludakit:fluda-jdbc-client-core",
                        "io.github.fludakit:fluda-jdbc-client-cdi",
                        "io.github.fludakit:fluda-jdbc-client-config",
                        "io.github.fludakit:fluda-sql-init-core",
                        "io.github.fludakit:fluda-sql-init-cdi",
                        "io.github.fludakit:fluda-sql-init-config"
                )
                .withoutTransitivity()
                .asFile();
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test-sqlinit.war")
                .addAsLibraries(libs)
                .addClasses(DataSourceProducer.class,
                        Engineer.class)
                .addAsResource("db/migration/V1__create_engineers.sql")
                .addAsResource("db/migration/V2__data_seeds.sql")
                .addAsResource("META-INF/microprofile-config.properties", "META-INF/microprofile-config.properties")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "deployment archive: {0}", archive.toString(true));
        return archive;
    }

    @Test
    public void verifyMigrationCreatedTable() {
        List<Engineer> engineers = jdbcClient.sql("SELECT id, dev_name FROM engineers ORDER BY id")
                .query(Engineer.class)
                .list();
        Assertions.assertEquals(2, engineers.size(), "Migration should have inserted 2 engineers");
        Assertions.assertEquals("Duke Jakarta", engineers.get(0).devName());
        Assertions.assertEquals("Arquillian Glassfish", engineers.get(1).devName());
    }
}
