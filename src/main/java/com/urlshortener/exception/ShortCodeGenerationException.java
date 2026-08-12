package com.urlshortener.exception;

/**
 * Thrown when a unique short code could not be generated within the bounded retry limit.
 * Given a 7-character Base62 space (62^7 ≈ 3.5 trillion codes), this should be effectively
 * unreachable in practice; if it does occur, it signals something worth investigating (e.g. a
 * broken generator, or the table approaching the size where collisions become likely), not a
 * client input problem — hence a 500, not a 400 (see {@link GlobalExceptionHandler}).
 */
public class ShortCodeGenerationException extends RuntimeException {

    public ShortCodeGenerationException(String message) {
        super(message);
    }
}
