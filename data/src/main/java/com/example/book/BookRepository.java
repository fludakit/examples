package com.example.book;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

/** CRUD over {@link Book}; each read JOIN-projects the composing {@code author}, each write persists it. */
@Repository
public interface BookRepository extends CrudRepository<Book, Long> { }
