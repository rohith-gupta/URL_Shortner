package com.urlshortener.exception;

/**
 * Thrown when a caller-supplied short code (custom alias) is already in use. Maps to
 * {@code 409} — see {@link GlobalExceptionHandler}. Deliberately distinct from {@link
 * ShortCodeGenerationException} (which is about the system failing to find a free
 * <em>randomly generated</em> code, a 500-worthy internal condition): a colliding custom
 * alias is an ordinary, expected client-facing conflict, not a system failure, hence 409 not
 * 500, and hence no retry — see {@code UrlService#createWithCustomAlias}.
 */
public class ShortCodeAlreadyExistsException extends RuntimeException {

    public ShortCodeAlreadyExistsException(String shortCode) {
        super("Short code '" + shortCode + "' is already in use");
    }
}
