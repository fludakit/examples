package com.example.task;

import io.github.fludakit.jdbc.JdbcClient;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    /** The escape hatch: a hand-written query run straight through the injected client. */
    JdbcClient getJdbcClient();

    /**
     * A {@code default} method is NOT abstract, so the processor skips generating it — the body below
     * is the implementation, using the generated {@link TaskRowMapper} and the {@link #getJdbcClient()}
     * accessor to run raw SQL.
     */
    default Task[] findByStatus(Status status) {
        return getJdbcClient()
                .sql("SELECT \"id\", \"title\", \"status\" FROM \"task\" WHERE \"status\" = :status")
                .param("status", status == null ? null : status.name())
                .query(new TaskRowMapper())
                .list()
                .toArray(new Task[0]);
    }

    /** CONTAINS derived from the name: an {@code LIKE '%text%'} predicate on {@code title}. */
    List<Task> findByTitleContains(String text);

    /**
     * LIKE derived from the name, with the caller's pattern used verbatim, cut into an offset page:
     * the {@link PageRequest} parameter plus the {@link Page} return drive the generated paging.
     */
    Page<Task> findByTitleLike(String titlePattern, PageRequest pageRequest);
}
