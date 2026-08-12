package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response body for {@code GET /api/urls/{shortCode}/analytics}. Deliberately independent of
 * {@link com.urlshortener.entity.UrlMapping} — the persistence entity is never returned
 * directly from the controller.
 *
 * <p>Deliberately basic, by explicit scope decision (see docs/AI_WORKLOG.md, "Decision: Core
 * API scope" and the analytics-slice entry): {@code clickCount} only — no referrer, device, or
 * geographic breakdown. Those are candidates for later brownfield/ambiguous-requirement work,
 * not something this endpoint quietly grows into.
 */
public record UrlAnalyticsResponse(

        @Schema(description = "The 7-character Base62 short code", example = "aZ3kX9q")
        String shortCode,

        @Schema(description = "The full short URL a client can use to redirect", example = "http://localhost:8080/aZ3kX9q")
        String shortUrl,

        @Schema(description = "The original long URL that was shortened")
        String originalUrl,

        @Schema(description = "Number of successful redirects recorded via GET /{shortCode}", example = "42")
        long clickCount,

        @Schema(description = "When this short URL was created")
        Instant createdAt

) {
}
