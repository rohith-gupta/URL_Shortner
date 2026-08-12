package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response body for {@code GET /api/urls/{shortCode}}. Deliberately independent of {@link
 * com.urlshortener.entity.UrlMapping} — the persistence entity is never returned directly from
 * the controller. Deliberately excludes {@code clickCount}: that's the analytics endpoint's
 * concern, not this one's (see docs/AI_WORKLOG.md, "Decision: Core API scope").
 */
public record UrlDetailsResponse(

        @Schema(description = "The original long URL that was shortened")
        String originalUrl,

        @Schema(description = "The 7-character Base62 short code", example = "aZ3kX9q")
        String shortCode,

        @Schema(description = "The full short URL a client can use to redirect", example = "http://localhost:8080/aZ3kX9q")
        String shortUrl,

        @Schema(description = "When this short URL was created")
        Instant createdAt

) {
}
