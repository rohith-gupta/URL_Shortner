package com.urlshortener.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link UrlMapping#isExpired(Instant)} — the one place expiration logic
 * is centralized (see docs/AI_WORKLOG.md, ambiguous-requirement scenario "Shortened URLs
 * should expire"). No mocks, no Spring context needed: both sides of the comparison are
 * explicit {@link Instant} values, so the exact boundary is deterministically testable here in
 * a way it wouldn't be against a real or even fixed clock elsewhere.
 */
class UrlMappingTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void nullExpiresAt_isNeverExpired() {
        UrlMapping mapping = new UrlMapping("https://example.com", "abc1234", null);

        assertThat(mapping.isExpired(NOW)).isFalse();
    }

    @Test
    void expiresAtInTheFuture_isNotExpired() {
        UrlMapping mapping = new UrlMapping("https://example.com", "abc1234", NOW.plusSeconds(1));

        assertThat(mapping.isExpired(NOW)).isFalse();
    }

    @Test
    void expiresAtInThePast_isExpired() {
        UrlMapping mapping = new UrlMapping("https://example.com", "abc1234", NOW.minusSeconds(1));

        assertThat(mapping.isExpired(NOW)).isTrue();
    }

    @Test
    void expiresAtExactlyNow_isExpired() {
        // "expires at 6pm" reads as no-longer-valid starting at 6pm, not valid-through-6pm.
        UrlMapping mapping = new UrlMapping("https://example.com", "abc1234", NOW);

        assertThat(mapping.isExpired(NOW)).isTrue();
    }
}
