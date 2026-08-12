package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Core URL-shortening API. Only creation is implemented here — see docs/AI_WORKLOG.md and
 * docs/REQUIREMENTS.md for what's intentionally deferred (redirect, details, analytics, etc.).
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

        // Location points at the (future) GET /api/urls/{shortCode} resource, not the public
        // redirect entry point GET /{shortCode} — those are different resources with different
        // semantics, and only the former is "the resource this POST created" in REST terms.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{shortCode}")
                .buildAndExpand(response.shortCode())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
