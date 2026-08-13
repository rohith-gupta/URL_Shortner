package com.urlshortener.dto;

import com.urlshortener.dto.validation.HttpUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Request body for {@code POST /api/urls}.
 */
public record CreateUrlRequest(

        @Schema(description = "The long URL to shorten", example = "https://example.com/some/very/long/path")
        @NotBlank(message = "originalUrl is required")
        @Size(max = 2048, message = "originalUrl must be at most 2048 characters")
        @HttpUrl(message = "originalUrl must be a syntactically valid http or https URL")
        String originalUrl,

        @Schema(
                description = "Optional custom short code. If omitted, a random 7-character "
                        + "code is generated (existing behavior, unchanged). 4-30 characters: "
                        + "letters, digits, hyphen (-), or underscore (_) only.",
                example = "products",
                nullable = true
        )
        @Pattern(
                regexp = "^[0-9a-zA-Z_-]{4,30}$",
                message = "customAlias must be 4-30 characters using only letters, digits, hyphen (-), or underscore (_)"
        )
        String customAlias,

        @Schema(
                description = "Optional absolute expiration timestamp (UTC). If omitted, the "
                        + "short URL never expires (existing behavior, unchanged). Must be "
                        + "strictly in the future.",
                example = "2026-08-20T18:00:00Z",
                nullable = true
        )
        @Future(message = "expiresAt must be strictly in the future")
        Instant expiresAt

) {
}
