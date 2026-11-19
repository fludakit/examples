package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import io.github.fludakit.jdbc.support.GeneratedKeyHolder;
import io.github.fludakit.jdbc.support.KeyHolder;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EngineerService {

    @Inject
    JdbcClient client;

    public List<Engineer> findAll() {
        return client.sql("SELECT id, name FROM engineers ORDER BY id")
                .query(Engineer.class)
                .list();
    }

    public Engineer findById(Long id) {
        return client.sql("SELECT id, name FROM engineers WHERE id = :id")
                .param("id", id)
                .query(Engineer.class)
                .single();
    }

    public long create(String name) {
        KeyHolder holder = new GeneratedKeyHolder();
        client.sql("INSERT INTO engineers (name) VALUES (:name)")
                .param("name", name)
                .update(holder);
        return ((Number) holder.getKey()).longValue();
    }

    public void update(Long id, String name) {
        client.sql("UPDATE engineers SET name = :name WHERE id = :id")
                .param("name", name)
                .param("id", id)
                .update();
    }

    public void delete(Long id) {
        client.sql("DELETE FROM engineers WHERE id = :id")
                .param("id", id)
                .update();
    }
}
