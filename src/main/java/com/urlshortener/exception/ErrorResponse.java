package com.urlshortener.exception;

import java.time.Instant;
import java.util.List;

/**
 * Uniform JSON error body returned by {@link GlobalExceptionHandler}. Never carries a stack
 * trace or other internal implementation detail — only what a client needs to understand and
 * fix (or report) the problem.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {
    }
}
