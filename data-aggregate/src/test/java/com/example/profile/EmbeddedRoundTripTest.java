package com.example.profile;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trips an {@code @ValueObject} embedded in an aggregate through the generated flattened
 * columns: writes bind each {@code address_*} leaf path, reads rebuild the VO tree leaf-by-leaf.
 * Presence rule: an embedded VO is reconstructed iff at least one flattened boxed leaf is non-null —
 * a NULL database row maps back to a {@code null} property, not an all-null VO.
 */
@ExtendWith(WeldJunit5Extension.class)
class EmbeddedRoundTripTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            ProfileRepositoryImpl.class).build();

    @Inject ProfileRepository profiles;

    @Test
    void embeddedRoundTrip() {
        Profile p = new Profile();
        Address a = new Address();
        a.setCity("Springfield");
        a.setZip(4915);
        GeoPoint gp = new GeoPoint();
        gp.setLat(39.8);
        gp.setLng(-83.9);
        a.setPoint(gp);
        p.setAddress(a);

        Profile saved = profiles.save(p);
        Profile found = profiles.findById(saved.getId()).orElseThrow();
        assertThat(found.getAddress().getCity()).isEqualTo("Springfield");
        assertThat(found.getAddress().getZip()).isEqualTo(4915);
        assertThat(found.getAddress().getPoint().getLat()).isEqualTo(39.8);
    }

    @Test
    void nestedChildWithAllNullLeavesStaysNull() {
        Profile p = new Profile();
        Address a = new Address();
        a.setCity("Springfield");
        a.setZip(4915);
        p.setAddress(a);

        Profile saved = profiles.save(p);
        Profile found = profiles.findById(saved.getId()).orElseThrow();
        assertThat(found.getAddress().getCity()).isEqualTo("Springfield");
        assertThat(found.getAddress().getZip()).isEqualTo(4915);
        assertThat(found.getAddress().getPoint()).isNull();
    }

    @Test
    void nestedChildWithLeavesSetIsReconstructed() {
        Profile p = new Profile();
        Address a = new Address();
        GeoPoint gp = new GeoPoint();
        gp.setLat(39.8);
        gp.setLng(-83.9);
        a.setPoint(gp);
        p.setAddress(a);

        Profile saved = profiles.save(p);
        Profile found = profiles.findById(saved.getId()).orElseThrow();
        assertThat(found.getAddress().getPoint().getLat()).isEqualTo(39.8);
        assertThat(found.getAddress().getPoint().getLng()).isEqualTo(-83.9);
        assertThat(found.getAddress().getCity()).isNull();
        assertThat(found.getAddress().getZip()).isNull();
    }

    @Test
    void fullyNullEmbeddedStaysNull() {
        Profile p = new Profile();
        Profile saved = profiles.save(p);
        Profile found = profiles.findById(saved.getId()).orElseThrow();
        assertThat(found.getAddress()).isNull();
    }
}
