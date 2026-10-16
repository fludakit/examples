package com.example.task;

import com.example.DataSourceProducer;
import io.github.fludakit.jdbc.cdi.ConverterRegistryProducer;
import io.github.fludakit.jdbc.cdi.JdbcClientProducer;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(WeldJunit5Extension.class)
class TaskEnumTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator.from(
            DataSourceProducer.class,
            JdbcClientProducer.class,
            ConverterRegistryProducer.class,
            TaskRepositoryImpl.class).build();

    @Inject TaskRepository tasks;

    @BeforeEach
    void seed() {
        tasks.getJdbcClient().sql("DELETE FROM \"task\"").update();
        insert("Ship it", Status.IN_PROGRESS);
        insert("Triage", Status.OPEN);
        insert("Archive", Status.DONE);
    }

    @Test
    void enumFieldRoundTripsThroughGeneratedMapper() {
        Task t = new Task();
        t.setTitle("Deploy");
        t.setStatus(Status.DONE);
        Task saved = tasks.insert(t);
        assertThat(saved.getId()).isNotNull();

        Task reloaded = tasks.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("Deploy");
        assertThat(reloaded.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void defaultFindByStatusRunsTheHandWrittenQuery() {
        Task[] openTasks = tasks.findByStatus(Status.OPEN);
        assertThat(openTasks).extracting(Task::getTitle).containsExactly("Triage");
        assertThat(openTasks).allSatisfy(task -> {
            assertThat(task.getStatus()).isEqualTo(Status.OPEN);
            assertThat(task.getTitle()).isNotBlank();
        });

        Task[] doneTasks = tasks.findByStatus(Status.DONE);
        assertThat(doneTasks).extracting(Task::getTitle).containsExactly("Archive");
    }

    @Test
    void containsFinderIsDerivedFromTheMethodName() {
        List<Task> found = tasks.findByTitleContains("i");
        assertThat(found).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Ship it", "Triage", "Archive");

        assertThat(tasks.findByTitleContains("zzz")).isEmpty();
    }

    @Test
    void likeFinderTakesThePatternVerbatimAndPages() {
        Page<Task> page = tasks.findByTitleLike("%Ship%", PageRequest.ofPage(1).size(10));
        assertThat(page.content()).extracting(Task::getTitle).containsExactly("Ship it");

        Page<Task> all = tasks.findByTitleLike("%%", PageRequest.ofPage(1).size(2));
        assertThat(all.content()).hasSize(2);
    }

    private void insert(String title, Status status) {
        Task t = new Task();
        t.setTitle(title);
        t.setStatus(status);
        tasks.insert(t);
    }
}
