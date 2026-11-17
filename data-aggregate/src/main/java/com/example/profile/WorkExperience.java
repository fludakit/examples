package com.example.profile;

import io.github.fludakit.data.annotation.Association;
import io.github.fludakit.data.annotation.Entity;
import io.github.fludakit.data.annotation.Identity;

/**
 * Composition entity-collection target of {@code Profile.workHistory}. The bare {@code profile}
 * inverse field classifies as a non-composing reference on {@code profile_id}. {@code company} is
 * an {@code @Association}: the aggregate stores {@code company_id} and does not insert the company.
 */
@Entity
public class WorkExperience {
    @Identity Long id;
    @Association Company company;
    int yearStart;
    int yearEnd;
    Profile profile;

    public WorkExperience() {}

    public WorkExperience(Company company, int yearStart, int yearEnd) {
        this.company = company;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public int getYearStart() { return yearStart; }
    public void setYearStart(int yearStart) { this.yearStart = yearStart; }
    public int getYearEnd() { return yearEnd; }
    public void setYearEnd(int yearEnd) { this.yearEnd = yearEnd; }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }
}
