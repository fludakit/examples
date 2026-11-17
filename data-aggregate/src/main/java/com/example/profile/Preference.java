package com.example.profile;

import io.github.fludakit.data.annotation.Entity;
import io.github.fludakit.data.annotation.Identity;

/**
 * Composing to-one target of {@code Profile.preference}. The foreign key {@code preference_id}
 * lives on the profile table.
 */
@Entity
public class Preference {
    public enum Visibility { PUBLIC, CIRCLE, PRIVATE }

    @Identity Long id;
    Visibility visibility;
    String defaultTheme;

    public Preference() {}

    public Preference(Visibility visibility, String defaultTheme) {
        this.visibility = visibility;
        this.defaultTheme = defaultTheme;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public String getDefaultTheme() { return defaultTheme; }
    public void setDefaultTheme(String defaultTheme) { this.defaultTheme = defaultTheme; }
}
