package com.example.team;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trips both entity-relationship kinds through the generated hydration. Both fixtures are
 * {@code @Entity}, so the many-to-one {@code Player.team} side is an association materialised id-only,
 * and a partial parent reference is NOT cascaded on update.
 */
@ExtendWith(WeldJunit5Extension.class)
class TeamPlayerHydrationTest {

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
    void relationshipRoundTrip() {
        Team team = new Team();
        team.setName("Falcons");
        Team savedTeam = teams.save(team);

        Player striker = new Player();
        striker.setName("Ramos");
        striker.setTeam(savedTeam);                       // MANY_TO_ONE write binds team_id via getId
        Player keeper = new Player();
        keeper.setName("Navas");
        keeper.setTeam(savedTeam);
        players.save(striker);
        players.save(keeper);

        Team found = teams.findById(savedTeam.getId()).orElseThrow();
        assertThat(found.getPlayers()).extracting(Player::getName)
                .containsExactlyInAnyOrder("Ramos", "Navas");
        assertThat(found.getPlayers()).allSatisfy(p -> {
            assertThat(p.getTeam()).isNotNull();
            assertThat(p.getTeam().getId()).isEqualTo(savedTeam.getId());
            assertThat(p.getTeam().getName()).isNull();
            assertThat(p.getTeam().getPlayers()).isEmpty();
        });

        Player foundPlayer = players.findById(striker.getId()).orElseThrow();
        assertThat(foundPlayer.getTeam()).isNotNull();
        assertThat(foundPlayer.getTeam().getId()).isEqualTo(savedTeam.getId());
        assertThat(foundPlayer.getTeam().getName())
                .as("bare entity reference inside @Entity materialises as an association (id-only)")
                .isNull();
        assertThat(foundPlayer.getTeam().getPlayers()).isEmpty();      // depth-1 the other way

        foundPlayer.getTeam().setName("Falcons II");                   // write via partial ref
        players.update(foundPlayer);
        assertThat(teams.findById(savedTeam.getId()).orElseThrow().getName()).isEqualTo("Falcons");
    }
}
