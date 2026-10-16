package com.example.team;

import io.github.fludakit.data.annotation.Entity;
import io.github.fludakit.data.annotation.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * One-to-many owner: {@code players} is a bare {@code List<Player>} of entities, so it classifies as
 * an entity collection and reads group rows from the player's OWN table by the {@code team_id} fk.
 * Being {@code @Entity} (not {@code @AggregateRoot}), the composition WRITE cascade does NOT apply —
 * players are managed through {@link PlayerRepository}, and {@code teams.save} never touches them.
 */
@Entity
public class Team {
    @Identity Long id;
    String name;
    List<Player> players = new ArrayList<>();

    public Team() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
}
