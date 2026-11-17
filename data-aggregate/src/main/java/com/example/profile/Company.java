package com.example.profile;

import io.github.fludakit.data.annotation.Entity;
import io.github.fludakit.data.annotation.Identity;

/**
 * Company referenced by {@link WorkExperience} through {@code @Association}. The association does
 * not own the company's lifecycle: save the company first, then point a work experience at it.
 */
@Entity
public class Company {
    @Identity Long id;
    String name;
    String industry;

    public Company() {}

    public Company(String name, String industry) {
        this.name = name;
        this.industry = industry;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
}
