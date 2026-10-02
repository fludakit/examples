package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import io.github.fludakit.sqlinit.cdi.SqlInitBootstrapper;
import io.github.fludakit.tx.cdi.TransactionalCdiExtension;
import io.github.fludakit.tx.cdi.TransactionalInterceptor;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import javax.sql.DataSource;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the Java SE example with CDI, JDBC client, SQL init, and transactional support.
 * Uses {@link WeldInitiator} to explicitly declare all beans and extensions.
 */
@ExtendWith(WeldJunit5Extension.class)
class JavaSeExampleTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator
            .from(JdbcClientProducer.class, ConverterRegistryProducer.class, DataSourceProducer.class,
                    EngineerService.class, SqlInitBootstrapper.class, TransactionalInterceptor.class,
                    TransactionalCdiExtension.class)
            .build();

    @Inject
    JdbcClient client;

    @Inject
    DataSource dataSource;

    @Inject
    EngineerService service;

    @BeforeEach
    void setUp() throws Exception {
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM engineers");
        }
    }

    @Test
    void crud() {
        assertEquals(1, client.sql("INSERT INTO engineers (name) VALUES (:name)").param("name", "Ada").update());

        List<Engineer> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("Ada", all.get(0).name());

        Engineer ada = service.findById(all.get(0).id());
        assertEquals("Ada", ada.name());

        service.update(ada.id(), "Ada Lovelace");
        Engineer updated = service.findById(ada.id());
        assertEquals("Ada Lovelace", updated.name());

        service.delete(ada.id());
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void transactionalRollback() {
        assertThrows(IllegalStateException.class, () -> service.createTwo("Ada", null));
        assertTrue(service.findAll().isEmpty(), "both inserts should be rolled back");
    }
}
