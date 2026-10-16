package com.example.profile;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends CrudRepository<Profile, Long> {

    // Must stay a literal, not _Profile.ADDRESS_CITY: the processor proxies this annotation in the
    // round that still generates _Profile, so javac cannot inline the constant reference.
    @Find
    @OrderBy("address.city")
    List<Profile> orderedByCity();

    // Predicate THROUGH the composition entity collection: the camel path resolves its head
    // "workHistory" (the EntityCollection) and its leaf "company" (a plain column of
    // WorkExperience), and renders an EXISTS semi-join — never a LEFT JOIN — so a root cannot fan out.
    @Find
    List<Profile> findByWorkHistoryCompany(@By("workHistory.company") String company);

    // The unannotated COUNT stays a query-by-method name: it is resolved from the method name and is
    // the reason fluda-data-qbn is on this module's annotation-processor path.
    long countByWorkHistoryCompany(String company);
}
