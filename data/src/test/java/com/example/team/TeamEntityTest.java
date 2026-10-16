package com.example.team;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.data.Order;
import jakarta.data.page.CursoredPage;
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
 * The team fixtures are {@code @Entity} rather than {@code @AggregateRoot}: the bare {@code Player.team}
 * field is a NON-composing association (id-only partial on reads), the {@code Team.players} entity
 * collection keeps its stereotype-independent read loader, and there is NO composition write cascade.
 */
@ExtendWith(WeldJunit5Extension.class)
class TeamEntityTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            TeamRepositoryImpl.class,
            PlayerRepositoryImpl.class).build();

    @Inject TeamRepository teams;
    @Inject PlayerRepository players;

    @Test
    void bareReferenceInsideEntityMaterialisesIdOnlyPartial() {
        Team saved = teams.save(team("Falcons"));
        Player striker = players.save(player("Ramos", saved));

        Player back = players.findById(striker.getId()).orElseThrow();
        assertThat(back.getTeam()).isNotNull();
        assertThat(back.getTeam().getId()).isEqualTo(saved.getId());
        assertThat(back.getTeam().getName())
                .as("bare entity reference inside @Entity materialises as an association (id-only)")
                .isNull();
    }

    @Test
    void entityCollectionOnEntityOwnerStillHydratesOnRead() {
        Team saved = teams.save(team("Falcons"));
        players.save(player("Ramos", saved));
        players.save(player("Navas", saved));

        Team back = teams.findById(saved.getId()).orElseThrow();
        assertThat(back.getPlayers()).extracting(Player::getName)
                .containsExactlyInAnyOrder("Ramos", "Navas");
        assertThat(back.getPlayers()).allSatisfy(p -> {
            assertThat(p.getTeam()).isNotNull();
            assertThat(p.getTeam().getId()).isEqualTo(saved.getId());
            assertThat(p.getTeam().getName()).isNull();
            assertThat(p.getTeam().getPlayers()).isEmpty();
        });
    }

    @Test
    void updateOnEntityOwnerDoesNotCascadeEntityCollectionWrites() {
        Team saved = teams.save(team("Falcons"));
        Player striker = players.save(player("Ramos", saved));

        Team fresh = new Team();
        fresh.setId(saved.getId());
        fresh.setName("Falcons II");
        teams.update(fresh);

        assertThat(players.findById(striker.getId())).isPresent();
        Team back = teams.findById(saved.getId()).orElseThrow();
        assertThat(back.getName()).isEqualTo("Falcons II");
        assertThat(back.getPlayers()).extracting(Player::getName).containsExactly("Ramos");
    }

    @Test
    void runtimeOrderThroughEntityCollectionContributesJoin() {
        Team borussia = teams.save(team("Borussia"));
        Team ajax = teams.save(team("Ajax"));
        Team empty = teams.save(team("Empty"));
        players.save(player("Zeta", borussia));
        players.save(player("Alpha", ajax));

        Page<Team> page = teams.findAll(
                PageRequest.ofPage(1).size(10), Order.by(_Team.players.name.asc()));

        List<String> names = page.content().stream().map(Team::getName).toList();
        assertThat(names).contains(empty.getName());
        int ajaxAt = names.indexOf("Ajax");
        int borussiaAt = names.indexOf("Borussia");
        assertThat(ajaxAt).isNotNegative();
        assertThat(borussiaAt).isNotNegative();
        assertThat(ajaxAt).isLessThan(borussiaAt);
    }

    @Test
    void finderThroughEntityCollectionFiltersByJoinedPredicate() {
        Team falcons = teams.save(team("Falcons"));
        Team eagles = teams.save(team("Eagles"));
        players.save(player("Ramos", falcons));
        players.save(player("Silva", eagles));

        List<Team> found = teams.findByPlayersName("Ramos");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(falcons.getId());
        assertThat(found.get(0).getName()).isEqualTo("Falcons");
    }

    @Test
    void cursorModeRelationSortSeeksOnJoinQualifiedPredicate() {
        Team falcons = teams.save(team("Falcons"));
        Team eagles = teams.save(team("Eagles"));
        players.save(player("Navas", falcons));
        players.save(player("Ramos", eagles));

        PageRequest beyond = PageRequest.ofPage(1).size(10)
                .afterCursor(PageRequest.Cursor.forKey("Zzz", 0L));
        CursoredPage<Team> page = teams.orderedByPlayerName(beyond);
        assertThat(page.content()).isEmpty();
    }

    @Test
    void cursorModeRelationSortWalksTwoPages() {
        Team falcons = teams.save(team("Falcons"));
        players.save(player("zeta", falcons));
        players.save(player("alpha", falcons));   // MIN = "alpha"
        Team hawks = teams.save(team("Hawks"));
        players.save(player("beta", hawks));      // MIN = "beta"

        CursoredPage<Team> first = teams.orderedByPlayerName(
                PageRequest.ofPage(1).size(1).afterCursor(PageRequest.Cursor.forKey("A", 0L)));
        assertThat(first.content()).extracting(Team::getName).containsExactly("Falcons");

        CursoredPage<Team> second = teams.orderedByPlayerName(first.nextPageRequest());
        assertThat(second.content()).extracting(Team::getName).containsExactly("Hawks");
    }

    private static Team team(String name) {
        Team t = new Team();
        t.setName(name);
        return t;
    }

    private static Player player(String name, Team team) {
        Player p = new Player();
        p.setName(name);
        p.setTeam(team);
        return p;
    }
}
