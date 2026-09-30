package com.example;

/**
 * A row in the {@code engineers} table, mapped by the client's reflective record mapper.
 */
public record Engineer(Long id, String devName) {
}
