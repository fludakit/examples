package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;

@ArquillianTest
public class JdbcClientInjectionIT {
    private static final Logger LOGGER = Logger.getLogger(JdbcClientInjectionIT.class.getName());

    @Inject
    private JdbcClient jdbcClient;

    @Deployment
    public static WebArchive createDeployment() {
        var libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.postgresql:postgresql",
                        "io.github.fludakit:fluda-jdbc-client-core",
                        "io.github.fludakit:fluda-jdbc-client-cdi",
                        "io.github.fludakit:fluda-jdbc-client-config"
                )
                .withoutTransitivity()
                .asFile();
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test-jdbcclient-injection.war")
                .addAsLibraries(libs)
                .addClasses(DataSourceProducer.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "deployment archive: {0}", archive.toString(true));
        return archive;
    }

    @Test
    public void jdbcClientInjectionSelectOne() {
        Assertions.assertNotNull(jdbcClient, "JdbcClient should be injected");
        int one = jdbcClient.sql("SELECT 1").singleValue(Integer.class);
        Assertions.assertEquals(1, one);
    }
}
