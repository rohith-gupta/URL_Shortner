package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Public-facing redirect entry point — the actual short link a user clicks, as opposed to the
 * {@code /api/urls} management API. Deliberately a separate controller: different resource,
 * different semantics (see {@code CLAUDE.md}, API design).
 */
@RestController
@Tag(name = "Redirect", description = "Resolve a short code and redirect to the original URL")
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(
            summary = "Redirect to the original URL",
            description = "Resolves a 7-character Base62 short code and redirects (302) to the "
                    + "original URL, incrementing its click count. 302 (not 301) is used "
                    + "deliberately, so browsers/proxies don't cache the redirect and bypass "
                    + "this service — and its click counting — on subsequent visits."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to the original URL"),
            @ApiResponse(responseCode = "404", description = "Unknown or malformed short code")
    })
    @GetMapping("/{shortCode:[0-9a-zA-Z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.resolveAndRecordRedirect(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
