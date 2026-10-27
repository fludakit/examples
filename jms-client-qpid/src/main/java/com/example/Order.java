package com.example;

import java.io.Serializable;

/**
 * A serializable payload: the default {@code SimpleMessageConverter} turns it into an
 * {@code ObjectMessage}.
 */
public record Order(String id, String item, int quantity) implements Serializable {
}
