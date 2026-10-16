package com.example;

import com.example.profile.Address;
import com.example.profile.ContactPoint;
import com.example.profile.Education;
import com.example.profile.Profile;
import com.example.profile.ProfileRepository;
import com.example.profile.WorkExperience;
import com.example.profile._Profile;
import jakarta.data.Order;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Boots Weld SE and drives the CV {@code @AggregateRoot}: the embedded address flattens to columns,
 * the composition entity collections cascade on save, and the element collection round-trips through
 * the child table. Run with {@code mvn -pl data-aggregate exec:java}.
 */
public class Main {

    public static void main(String[] args) {
        try (WeldContainer container = new Weld().initialize()) {
            ProfileRepository profiles = container.select(ProfileRepository.class).get();

            Profile ada = new Profile();
            ada.setFullName("Ada Lovelace");
            Address address = new Address();
            address.setCity("London");
            address.setZip(12345);
            ada.setAddress(address);
            ada.getContacts().add(new ContactPoint("EMAIL", "ada@example.com"));
            ada.getWorkHistory().add(new WorkExperience("Analytical Engines", 1842, 1843));
            ada.getEducation().add(new Education("University of London", 1840));
            Profile saved = profiles.save(ada);

            Profile back = profiles.findById(saved.getId()).orElseThrow();
            System.out.println("address city (VO)    = " + back.getAddress().getCity());
            System.out.println("workHistory (casc.)  = " + back.getWorkHistory().size());
            System.out.println("education (casc.)    = " + back.getEducation().size());
            System.out.println("contacts (element)   = " + back.getContacts().size());

            System.out.println("by city order        = "
                    + profiles.orderedByCity().get(0).getAddress().getCity());
            System.out.println("countThroughColl.    = " + profiles.countByWorkHistoryCompany("Analytical Engines"));

            Page<Profile> page = profiles.findAll(
                    PageRequest.ofPage(1).size(10), Order.by(_Profile.address.city.asc()));
            System.out.println("paged sorted by city = " + page.content().size());
        }
    }
}
