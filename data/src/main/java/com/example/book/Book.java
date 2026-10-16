package com.example.book;

import io.github.fludakit.data.annotation.AggregateRoot;
import io.github.fludakit.data.annotation.Identity;

/**
 * {@code author} is a bare entity field on an {@code @AggregateRoot} whose target is managed, so it
 * classifies as COMPOSING: every CRUD read LEFT JOIN-projects the author onto the book row, and a
 * write of the book writes its author too. A book with no author still comes back, null reference.
 */
@AggregateRoot
public class Book {
    @Identity Long id;
    String title;
    Author author;

    public Book() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
}
