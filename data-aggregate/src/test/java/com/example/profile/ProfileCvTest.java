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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flagship end-to-end pin for the CV aggregate: composition entity collections ({@code workHistory},
 * {@code education} — managed {@code @Entity} classes with their own repositories and a bare inverse
 * {@code Profile profile} field), an element collection of {@code @ValueObject} contacts, and
 * navigable static-metamodel sorts through both an embedded VO ({@code _Profile.address.city}) and an
 * entity collection ({@code _Profile.workHistory.company}).
 */
@ExtendWith(WeldJunit5Extension.class)
class ProfileCvTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            ProfileRepositoryImpl.class,
            WorkExperienceRepositoryImpl.class,
            EducationRepositoryImpl.class).build();

    @Inject ProfileRepository profiles;
    @Inject WorkExperienceRepository workExperiences;
    @Inject EducationRepository educations;

    @Test
    void aggregateRoundTripsAllCollections() {
        Profile p = new Profile();
        p.setFullName("Ada Lovelace");
        Address address = new Address();
        address.setCity("London");
        address.setZip(12345);
        p.setAddress(address);
        p.getContacts().add(new ContactPoint("EMAIL", "ada@example.com"));
        p.getContacts().add(new ContactPoint("PHONE", "+44-20-7946-0000"));
        Profile saved = profiles.save(p);

        workExperiences.save(withProfile(new WorkExperience("Analytical Engines", 1842, 1843), saved));
        Education edu = new Education("University of London", 1840);
        edu.setProfile(saved);
        educations.save(edu);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getFullName()).isEqualTo("Ada Lovelace");

        assertThat(back.getWorkHistory()).hasSize(1);
        assertThat(back.getWorkHistory().get(0).getCompany()).isEqualTo("Analytical Engines");
        assertThat(back.getWorkHistory().get(0).getYearStart()).isEqualTo(1842);
        assertThat(back.getEducation()).hasSize(1);
        assertThat(back.getEducation().get(0).getInstitution()).isEqualTo("University of London");
        assertThat(back.getEducation().get(0).getYear()).isEqualTo(1840);

        assertThat(back.getContacts()).hasSize(2);
        assertThat(back.getContacts()).extracting(ContactPoint::getKind)
                .containsExactlyInAnyOrder("EMAIL", "PHONE");

        WorkExperience member = back.getWorkHistory().get(0);
        assertThat(member.getId()).isNotNull();
        WorkExperience direct = workExperiences.findById(member.getId()).orElseThrow();
        assertThat(direct.getId()).isEqualTo(member.getId());
        assertThat(direct.getCompany()).isEqualTo("Analytical Engines");
        assertThat(direct.getProfile()).isNotNull();
        assertThat(direct.getProfile().getId()).isEqualTo(saved.getId());
        assertThat(direct.getProfile().getFullName()).isNull();
    }

    @Test
    void aggregateSaveCascadesCompositionEntityCollections() {
        Profile p = new Profile();
        p.setFullName("Ada Lovelace");
        p.getWorkHistory().add(new WorkExperience("Analytical Engines", 1842, 1843));
        p.getEducation().add(new Education("University of London", 1840));
        Profile saved = profiles.save(p);

        Profile back = profiles.findById(saved.getId()).orElseThrow();
        assertThat(back.getWorkHistory()).hasSize(1);
        assertThat(back.getWorkHistory().get(0).getCompany()).isEqualTo("Analytical Engines");
        assertThat(back.getWorkHistory().get(0).getYearStart()).isEqualTo(1842);
        assertThat(back.getWorkHistory().get(0).getYearEnd()).isEqualTo(1843);
        assertThat(back.getWorkHistory().get(0).getId()).isNotNull();
        assertThat(back.getEducation()).hasSize(1);
        assertThat(back.getEducation().get(0).getInstitution()).isEqualTo("University of London");
        assertThat(back.getEducation().get(0).getYear()).isEqualTo(1840);
        assertThat(back.getEducation().get(0).getId()).isNotNull();

        Long weId = back.getWorkHistory().get(0).getId();
        WorkExperience direct = workExperiences.findById(weId).orElseThrow();
        assertThat(direct.getId()).isEqualTo(weId);
        assertThat(direct.getCompany()).isEqualTo("Analytical Engines");

        profiles.save(back);
        Profile replaced = profiles.findById(saved.getId()).orElseThrow();
        assertThat(replaced.getWorkHistory()).hasSize(1);
        assertThat(replaced.getEducation()).hasSize(1);
    }

    @Test
    void sortThroughCollectionPathContributesJoin() {
        Profile ada = profile("Ada", "London");
        ada.getWorkHistory().add(new WorkExperience("Zeta Corp", 1842, 1843));
        Profile grace = profile("Grace", "New York");
        grace.getWorkHistory().add(new WorkExperience("Alpha Ltd", 1952, 1952));
        Profile rene = profile("Rene", "Paris");
        profiles.save(ada);
        profiles.save(grace);
        profiles.save(rene);

        Page<Profile> page = profiles.findAll(
                PageRequest.ofPage(1).size(10), Order.by(_Profile.workHistory.company.asc()));

        assertThat(page.content()).isNotEmpty();
        List<String> names = page.content().stream().map(Profile::getFullName).toList();
        assertThat(names).contains("Rene");
        int graceAt = names.indexOf("Grace");
        int adaAt = names.indexOf("Ada");
        assertThat(graceAt).isNotNegative();
        assertThat(adaAt).isNotNegative();
        assertThat(graceAt).isLessThan(adaAt);
    }

    @Test
    void navigableEmbeddedPathSortStillWorks() {
        profiles.save(profile("Beta", "Zürich"));
        profiles.save(profile("Alpha", "Aarau"));

        Page<Profile> page = profiles.findAll(
                PageRequest.ofPage(1).size(10), Order.by(_Profile.address.city.asc()));
        assertThat(page.content()).extracting(p -> p.getAddress().getCity())
                .containsExactly("Aarau", "Zürich");
    }

    @Test
    void toManySortAndPredicateReturnEachRootOnce() {
        Profile ada = profile("Ada", "London");
        ada.getWorkHistory().add(new WorkExperience("alpha", 1842, 1843));
        ada.getWorkHistory().add(new WorkExperience("zeta", 1843, 1844));
        Profile grace = profile("Grace", "New York");
        grace.getWorkHistory().add(new WorkExperience("Beta", 1952, 1952));
        profiles.save(ada);
        profiles.save(grace);

        Page<Profile> page = profiles.findAll(
                PageRequest.ofPage(1).size(10), Order.by(_Profile.workHistory.company.asc()));
        assertThat(page.content()).extracting(Profile::getFullName)
                .containsExactly("Grace", "Ada"); // "Beta" < "alpha"; each root once
        assertThat(page.content())
                .extracting(p -> p.getWorkHistory().stream().map(WorkExperience::getCompany)
                        .min(String::compareTo).orElseThrow())
                .containsExactly("Beta", "alpha");

        List<Profile> zetaHolders = profiles.findByWorkHistoryCompany("zeta");
        assertThat(zetaHolders).extracting(Profile::getFullName).containsExactly("Ada");
    }

    @Test
    void countThroughToManyPredicateIsDistinctRootTotal() {
        Profile ada = profile("Ada", "London");
        ada.getWorkHistory().add(new WorkExperience("alpha", 1842, 1843));
        ada.getWorkHistory().add(new WorkExperience("alpha", 1843, 1844));
        Profile grace = profile("Grace", "New York");
        grace.getWorkHistory().add(new WorkExperience("alpha", 1952, 1952));
        Profile rene = profile("Rene", "Paris");
        rene.getWorkHistory().add(new WorkExperience("zeta", 1900, 1901));
        profiles.save(ada);
        profiles.save(grace);
        profiles.save(rene);

        assertThat(profiles.countByWorkHistoryCompany("alpha")).isEqualTo(2L);
    }

    private static Profile profile(String fullName, String city) {
        Profile p = new Profile();
        p.setFullName(fullName);
        Address a = new Address();
        a.setCity(city);
        p.setAddress(a);
        return p;
    }

    private static WorkExperience withProfile(WorkExperience we, Profile profile) {
        we.setProfile(profile);
        return we;
    }
}
