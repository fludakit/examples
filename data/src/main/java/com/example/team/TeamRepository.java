package com.example.team;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends CrudRepository<Team, Long> {

    /**
     * A predicate THROUGH the entity collection: {@code @By("players.name")} resolves its head to the
     * collection and its leaf to a plain Player column, rendering an EXISTS semi-join (never a LEFT
     * JOIN), so a root cannot fan out with its children.
     */
    @Find
    List<Team> findByPlayersName(@By("players.name") String name);

    /**
     * Cursored paging ordered through a to-many relation: the {@code @OrderBy} path bakes the
     * aggregate sort key and the root-id tiebreak into the keyset seek.
     */
    @Find
    @OrderBy("players.name")
    CursoredPage<Team> orderedByPlayerName(PageRequest pageRequest);
}
