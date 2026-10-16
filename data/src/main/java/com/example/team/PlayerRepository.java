package com.example.team;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

/** Players are managed here directly; {@code teams.save} never cascades to them. */
@Repository
public interface PlayerRepository extends CrudRepository<Player, Long> { }
