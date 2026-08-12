package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.dto.UrlDetailsResponse;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Core URL-shortening management API — create and inspect. See docs/AI_WORKLOG.md and
 * docs/REQUIREMENTS.md for what's intentionally deferred (analytics, expiration, aliases,
 * update/delete, etc.). The public redirect entry point, {@code GET /{shortCode}}, is a
 * separate resource — see {@link RedirectController}.
 */
@RestController
@RequestMapping("/api/urls")
@Tag(name = "URLs", description = "Create and manage shortened URLs")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(
            summary = "Create a shortened URL",
            description = "Accepts a long http/https URL and returns a newly generated 7-character short code."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created"),
            @ApiResponse(responseCode = "400", description = "Invalid request body (missing/blank/malformed URL, or unsupported scheme)")
    })
    @PostMapping
    public ResponseEntity<CreateUrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        CreateUrlResponse response = urlService.createShortUrl(request.originalUrl());

        // Location points at the GET /api/urls/{shortCode} resource, not the public redirect
        // entry point GET /{shortCode} — those are different resources with different
        // semantics, and only the former is "the resource this POST created" in REST terms.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Get short URL details",
            description = "Returns metadata for a short code. Read-only: does not redirect and "
                    + "does not affect its click count. Click/redirect counts are exposed by "
                    + "the separate analytics endpoint, not here."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Short URL details"),
            @ApiResponse(responseCode = "404", description = "Unknown or malformed short code")
    })
    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")
    public UrlDetailsResponse getUrlDetails(@PathVariable String shortCode) {
        return urlService.getUrlDetails(shortCode);
    }

    @Operation(
            summary = "Get basic click-count analytics",
            description = "Returns the redirect (click) count for a short code, as recorded by "
                    + "GET /{shortCode}. Read-only: does not redirect and does not affect the "
                    + "count itself. Basic analytics only — no referrer/device/geography "
                    + "breakdown."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Basic analytics for the short URL"),
            @ApiResponse(responseCode = "404", description = "Unknown or malformed short code")
    })
    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}/analytics")
    public UrlAnalyticsResponse getUrlAnalytics(@PathVariable String shortCode) {
        return urlService.getUrlAnalytics(shortCode);
    }
}
