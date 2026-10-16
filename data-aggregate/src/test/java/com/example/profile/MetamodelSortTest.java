package com.example.profile;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Type-safe sorting through the generated {@code _Profile} static metamodel: runtime {@code Order}
 * criteria carry embedded dot paths that resolve to flattened columns, and a compile-time
 * {@code @OrderBy} dot path resolves the same way through the descriptor.
 */
@ExtendWith(WeldJunit5Extension.class)
class MetamodelSortTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            ProfileRepositoryImpl.class).build();

    @Inject ProfileRepository profiles;

    @Test
    void runtimeSortOnEmbeddedLeafOrdersByFlattenedColumn() {
        profiles.save(profile("Beta", 1.0));
        profiles.save(profile("Alpha", 3.0));
        profiles.save(profile("Gamma", 2.0));

        Page<Profile> byCity = profiles.findAll(
                PageRequest.ofSize(3), Order.by(_Profile.address.city.asc()));
        assertThat(byCity.content()).extracting(p -> p.getAddress().getCity())
                .containsExactly("Alpha", "Beta", "Gamma");

        Page<Profile> byLat = profiles.findAll(
                PageRequest.ofSize(3), Order.by(_Profile.address.point.lat.desc()));
        assertThat(byLat.content()).extracting(p -> p.getAddress().getPoint().getLat())
                .containsExactly(3.0, 2.0, 1.0);
    }

    @Test
    void orderByAnnotationResolvesEmbeddedDotPath() {
        profiles.save(profile("Beta", 1.0));
        profiles.save(profile("Alpha", 3.0));
        assertThat(profiles.orderedByCity()).extracting(p -> p.getAddress().getCity())
                .containsExactly("Alpha", "Beta");
    }

    private static Profile profile(String city, double lat) {
        Address a = new Address();
        a.setCity(city);
        GeoPoint gp = new GeoPoint();
        gp.setLat(lat);
        gp.setLng(0.0);
        a.setPoint(gp);
        Profile p = new Profile();
        p.setAddress(a);
        return p;
    }
}
