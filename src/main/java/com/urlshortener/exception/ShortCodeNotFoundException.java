package com.urlshortener.exception;

/**
 * Thrown when a short code doesn't resolve to any known mapping. Maps to {@code 404} — see
 * {@link GlobalExceptionHandler}. The message echoes back the short code the caller requested
 * (not an internal detail — it's exactly what they asked for), never anything about the
 * database or persistence layer.
 */
public class ShortCodeNotFoundException extends RuntimeException {

    public ShortCodeNotFoundException(String shortCode) {
        super("No short URL found for code '" + shortCode + "'");
    }
}
