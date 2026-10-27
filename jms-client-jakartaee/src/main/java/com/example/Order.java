package com.example;

/**
 * The payload of the example. It is sent as a plain text message: on a server, object messages need
 * provider-side deserialization configuration (and the classes must be visible to the provider), so
 * applications prefer text or JSON payloads.
 */
public record Order(String id, String item, int quantity) {

    /** The message body sent for this order. */
    public String text() {
        return id + ": " + item + " x " + quantity;
    }
}
