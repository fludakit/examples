package com.example.task;

import io.github.fludakit.data.annotation.Entity;
import io.github.fludakit.data.annotation.Identity;

/**
 * Enum round-trip corpus: {@code status} persists as its {@code name()} in a VARCHAR and the generated
 * row mapper reads it back via {@code Enum.valueOf}.
 */
@Entity
public class Task {
    @Identity Long id;
    String title;
    Status status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
