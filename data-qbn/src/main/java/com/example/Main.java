package com.example;

import com.example.task.Status;
import com.example.task.Task;
import com.example.task.TaskRepository;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.List;

/**
 * Boots Weld SE and drives the query-by-method-name {@link TaskRepository} through each feature this
 * example covers: the enum round-trip, the hand-written escape-hatch finder, and the two finders whose
 * predicates are derived from the method name. Run with {@code mvn -pl data-qbn exec:java}.
 */
public class Main {

    public static void main(String[] args) {
        try (WeldContainer container = new Weld().initialize()) {
            TaskRepository tasks = container.select(TaskRepository.class).get();

            tasks.insert(task("Ship it", Status.IN_PROGRESS));
            Task triage = tasks.insert(task("Triage", Status.OPEN));
            tasks.insert(task("Archive", Status.DONE));
            System.out.println("enum round-trip      = "
                    + tasks.findById(triage.getId()).orElseThrow().getStatus());

            System.out.println("findByStatus(OPEN)   = " + tasks.findByStatus(Status.OPEN).length);

            List<Task> contains = tasks.findByTitleContains("i");
            System.out.println("findByTitleContains  = " + contains.size());

            Page<Task> page = tasks.findByTitleLike("%Ship%", PageRequest.ofPage(1).size(10));
            System.out.println("findByTitleLike page = " + page.content().size());

            System.out.println("row count via hatch  = "
                    + tasks.getJdbcClient().sql("SELECT COUNT(*) FROM \"task\"").singleValue(Long.class));
        }
    }

    private static Task task(String title, Status status) {
        Task t = new Task();
        t.setTitle(title);
        t.setStatus(status);
        return t;
    }
}
