package com.urlshortener.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Centralized HTTP error handling. Every response body here is an {@link ErrorResponse} —
 * consistent shape, no stack traces, no internal exception messages leaked to the client for
 * unexpected failures.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Validation failed", request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Malformed request body", request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ErrorResponse> handleShortCodeGeneration(ShortCodeGenerationException ex,
                                                                     HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "Unable to create short URL at this time. Please try again.",
                request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(ShortCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShortCodeNotFound(ShortCodeNotFoundException ex,
                                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                Instant.now(), HttpStatus.NOT_FOUND.value(), "Not Found",
                ex.getMessage(), request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(ShortCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleShortCodeAlreadyExists(ShortCodeAlreadyExistsException ex,
                                                                        HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                Instant.now(), HttpStatus.CONFLICT.value(), "Conflict",
                ex.getMessage(), request.getRequestURI(), List.of()));
    }

    /**
     * Thrown by Spring's static-resource handling when a request path matches no controller
     * mapping at all — e.g. a malformed short code that doesn't satisfy
     * {@code RedirectController}'s route pattern. Without this handler, it would fall through
     * to {@link #handleUnexpected} and incorrectly become a {@code 500} instead of a
     * {@code 404}; explicitly handling it here keeps "no matching route" and "route matched but
     * nothing found" consistent in shape, and correct in status code.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                                 HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                Instant.now(), HttpStatus.NOT_FOUND.value(), "Not Found",
                "Resource not found", request.getRequestURI(), List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        // Logged server-side (with the real exception) precisely because the client never sees
        // more than the generic message below — this is the only place that detail is visible.
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "An unexpected error occurred.", request.getRequestURI(), List.of()));
    }
}
