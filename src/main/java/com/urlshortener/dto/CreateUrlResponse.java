package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response body for {@code POST /api/urls}. Deliberately independent of {@link
 * com.urlshortener.entity.UrlMapping} — the persistence entity is never returned directly from
 * the controller.
 */
public record CreateUrlResponse(

        @Schema(description = "The original long URL that was shortened")
        String originalUrl,

        @Schema(description = "The short code — a generated 7-character Base62 code, or the custom alias if one was supplied", example = "aZ3kX9q")
        String shortCode,

        @Schema(description = "The full short URL a client can use to redirect", example = "http://localhost:8080/aZ3kX9q")
        String shortUrl,

        @Schema(description = "When this short URL was created")
        Instant createdAt,

        @Schema(description = "When this short URL expires, or null if it never expires", nullable = true)
        Instant expiresAt

) {
}
