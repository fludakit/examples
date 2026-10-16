package com.example.book;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

/** Makes {@link Author} a managed entity, which is what turns {@code Book.author} into a composing reference. */
@Repository
public interface AuthorRepository extends CrudRepository<Author, Long> { }
