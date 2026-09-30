package com.example;

import io.github.fludakit.jdbc.JdbcClient;
import io.github.fludakit.jdbc.support.GeneratedKeyHolder;
import io.github.fludakit.jdbc.support.KeyHolder;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EngineerService {

    @Inject
    JdbcClient client;

    @Transactional
    public List<Engineer> findAll() {
        return client.sql("SELECT id, dev_name FROM engineers ORDER BY id")
                .query(Engineer.class)
                .list();
    }

    @Transactional
    public Engineer findById(Long id) {
        return client.sql("SELECT id, dev_name FROM engineers WHERE id = :id")
                .param("id", id)
                .query(Engineer.class)
                .single();
    }

    @Transactional
    public long create(String devName) {
        KeyHolder holder = new GeneratedKeyHolder();
        client.sql("INSERT INTO engineers (dev_name) VALUES (:devName)")
                .param("devName", devName)
                .update(holder);
        return ((Number) holder.getKey()).longValue();
    }

    @Transactional
    public void update(Long id, String devName) {
        client.sql("UPDATE engineers SET dev_name = :devName WHERE id = :id")
                .param("devName", devName)
                .param("id", id)
                .update();
    }

    @Transactional
    public void delete(Long id) {
        client.sql("DELETE FROM engineers WHERE id = :id")
                .param("id", id)
                .update();
    }
}
