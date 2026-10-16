package com.example.book;

import io.github.fludakit.data.annotation.AggregateRoot;
import io.github.fludakit.data.annotation.Identity;

/**
 * The managed target of the composing to-one projection: its own {@code @Repository} is what makes
 * {@link Book#author} a composing reference, and its {@code name} is what the JOIN-projected read
 * brings back FULL rather than as an id-only partial.
 */
@AggregateRoot
public class Author {
    @Identity Long id;
    String name;

    public Author() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
