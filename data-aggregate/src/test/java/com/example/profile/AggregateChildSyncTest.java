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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Write-side counterpart of the aggregate cascade: an <b>entity</b> collection has an identity, so
 * bringing its table in line with the in-memory collection is a <em>diff</em>, not a replace-all.
 * Each child is upserted (it keeps its identifier) and the parent's rows whose identifier the
 * collection no longer names are deleted. An <em>element</em> collection ({@code contacts}) has no
 * identity, so replace-all remains its only sync.
 */
@ExtendWith(WeldJunit5Extension.class)
class AggregateChildSyncTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            ProfileRepositoryImpl.class,
            WorkExperienceRepositoryImpl.class).build();

    @Inject ProfileRepository profiles;
    @Inject WorkExperienceRepository workExperiences;

    @Test
    void childIdentitiesSurviveASecondSave() {
        Profile ada = profile("Ada");
        ada.getWorkHistory().add(new WorkExperience("Analytical Engines", 1842, 1843));
        ada.getWorkHistory().add(new WorkExperience("Difference Engines", 1844, 1845));
        Profile saved = profiles.save(ada);
        List<Long> assigned = ids(saved.getWorkHistory());
        assertThat(assigned).doesNotContainNull();

        saved.setFullName("Ada Byron");
        profiles.save(saved);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getFullName()).isEqualTo("Ada Byron");
        assertThat(back.getWorkHistory()).hasSize(2);
        assertThat(ids(back.getWorkHistory())).containsExactlyInAnyOrderElementsOf(assigned);
    }

    @Test
    void childIdentitiesSurviveAnUpdate() {
        Profile ada = profile("Ada");
        ada.getWorkHistory().add(new WorkExperience("Analytical Engines", 1842, 1843));
        Profile saved = profiles.save(ada);
        Long childId = saved.getWorkHistory().get(0).getId();

        saved.getWorkHistory().get(0).setCompany("Analytical Engine, No. 2");
        profiles.update(saved);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getWorkHistory()).hasSize(1);
        assertThat(back.getWorkHistory().get(0).getId()).isEqualTo(childId);
        assertThat(back.getWorkHistory().get(0).getCompany()).isEqualTo("Analytical Engine, No. 2");
    }

    @Test
    void aChildDroppedFromTheCollectionLosesItsRow() {
        Profile ada = profile("Ada");
        ada.getWorkHistory().add(new WorkExperience("alpha", 1842, 1843));
        ada.getWorkHistory().add(new WorkExperience("zeta", 1844, 1845));
        Profile saved = profiles.save(ada);
        WorkExperience keep = child(saved, "alpha");
        WorkExperience drop = child(saved, "zeta");
        Long droppedId = drop.getId();

        saved.getWorkHistory().remove(drop);
        profiles.save(saved);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getWorkHistory()).hasSize(1);
        assertThat(back.getWorkHistory().get(0).getId()).isEqualTo(keep.getId());
        assertThat(workExperiences.findById(droppedId))
                .as("a child the collection no longer names must be deleted, not left behind")
                .isEmpty();
        assertThat(workExperiences.findById(keep.getId())).isPresent();
    }

    @Test
    void aNewChildIsInsertedAlongsideTheExistingOnes() {
        Profile ada = profile("Ada");
        ada.getWorkHistory().add(new WorkExperience("alpha", 1842, 1843));
        Profile saved = profiles.save(ada);
        Long firstId = saved.getWorkHistory().get(0).getId();

        saved.getWorkHistory().add(new WorkExperience("beta", 1846, 1847));
        profiles.save(saved);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getWorkHistory()).hasSize(2);
        assertThat(ids(back.getWorkHistory())).contains(firstId);
        assertThat(ids(back.getWorkHistory())).doesNotContainNull();
        assertThat(back.getWorkHistory()).extracting(WorkExperience::getCompany)
                .containsExactlyInAnyOrder("alpha", "beta");
    }

    @Test
    void emptyingTheCollectionRemovesEveryChildRow() {
        Profile ada = profile("Ada");
        ada.getWorkHistory().add(new WorkExperience("alpha", 1842, 1843));
        ada.getWorkHistory().add(new WorkExperience("zeta", 1844, 1845));
        Profile saved = profiles.save(ada);
        List<Long> assigned = ids(saved.getWorkHistory());

        saved.getWorkHistory().clear();
        profiles.save(saved);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getWorkHistory()).isEmpty();
        for (Long id : assigned) {
            assertThat(workExperiences.findById(id)).isEmpty();
        }
    }

    private static Profile profile(String fullName) {
        Profile p = new Profile();
        p.setFullName(fullName);
        return p;
    }

    private static WorkExperience child(Profile profile, String company) {
        return profile.getWorkHistory().stream()
                .filter(w -> company.equals(w.getCompany()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no child '" + company + "' in " + ids(profile.getWorkHistory())));
    }

    private static List<Long> ids(List<WorkExperience> children) {
        return children.stream().map(WorkExperience::getId).toList();
    }
}
