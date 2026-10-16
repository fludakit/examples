package com.example.team;

import io.github.fludakit.data.annotation.Entity;
import io.github.fludakit.data.annotation.Identity;

/**
 * Many-to-one side: the bare {@code Team team} field maps to the {@code team_id} column. On an
 * {@code @Entity} owner the field is a non-composing reference, so direct row reads fill only its
 * identifier and leave every other column null.
 */
@Entity
public class Player {
    @Identity Long id;
    String name;
    Team team;

    public Player() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
