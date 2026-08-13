package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.dto.UrlDetailsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlServiceTest {

    // Fixed, not real, "now" for the whole class — every test in this file is deterministic
    // with respect to time, not just the expiration-specific ones. See ClockConfig; here we
    // construct the fixed Clock directly rather than going through Spring.
    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    private UrlMappingRepository repository;
    private ShortCodeGenerator generator;
    private UrlService service;

    @BeforeEach
    void setUp() {
        repository = mock(UrlMappingRepository.class);
        generator = mock(ShortCodeGenerator.class);
        Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new UrlService(repository, generator, "http://localhost:8080", clock);
        // Default: repository.save just echoes back whatever entity it was given.
        when(repository.save(any(UrlMapping.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsShortUrlOnFirstAttempt_whenNoCollision() {
        when(generator.generate()).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com", null, null);

        assertThat(response.originalUrl()).isEqualTo("https://example.com");
        assertThat(response.shortCode()).isEqualTo("abc1234");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/abc1234");
        assertThat(response.createdAt()).isNotNull();
        verify(repository, times(1)).save(any(UrlMapping.class));
    }

    @Test
    void trimsOriginalUrlBeforePersisting() {
        when(generator.generate()).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);

        service.createShortUrl("  https://example.com  ", null, null);

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOriginalUrl()).isEqualTo("https://example.com");
    }

    @Test
    void retriesWithNewCodeWhenPreCheckFindsExistingCode() {
        when(generator.generate()).thenReturn("collide", "fresh01");
        when(repository.existsByShortCode("collide")).thenReturn(true);
        when(repository.existsByShortCode("fresh01")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com", null, null);

        assertThat(response.shortCode()).isEqualTo("fresh01");
        verify(generator, times(2)).generate();
        verify(repository, times(1)).save(any(UrlMapping.class));
        // The colliding candidate must never have reached save().
        verify(repository, times(0)).save(argThatShortCodeIs("collide"));
    }

    @Test
    void retriesWithNewCodeWhenDatabaseRejectsAsDuplicate() {
        // Pre-check passes both times (simulating a race lost between check and insert); the
        // database's UNIQUE constraint is what actually catches the first attempt.
        when(generator.generate()).thenReturn("raceLos", "afterRc");
        when(repository.existsByShortCode(any())).thenReturn(false);
        when(repository.save(argThatShortCodeIs("raceLos")))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));
        when(repository.save(argThatShortCodeIs("afterRc")))
                .thenAnswer(inv -> inv.getArgument(0));

        CreateUrlResponse response = service.createShortUrl("https://example.com", null, null);

        assertThat(response.shortCode()).isEqualTo("afterRc");
        verify(repository, times(2)).save(any(UrlMapping.class));
    }

    @Test
    void throwsAfterExhaustingBoundedRetries() {
        when(generator.generate()).thenReturn("aaaaaaa");
        when(repository.existsByShortCode("aaaaaaa")).thenReturn(true); // always collides

        assertThatThrownBy(() -> service.createShortUrl("https://example.com", null, null))
                .isInstanceOf(ShortCodeGenerationException.class);

        // Bounded: exactly 5 attempts, not unbounded looping.
        verify(generator, times(5)).generate();
        verify(repository, times(0)).save(any(UrlMapping.class));
    }

    private static UrlMapping argThatShortCodeIs(String shortCode) {
        return org.mockito.ArgumentMatchers.argThat(m -> m != null && shortCode.equals(m.getShortCode()));
    }

    // --- createShortUrl with customAlias (brownfield enhancement — see docs/AI_WORKLOG.md,
    // "Brownfield: add optional custom aliases to POST /api/urls") ---

    @Test
    void createShortUrl_withCustomAlias_usesAliasVerbatim_neverGeneratesRandomCode() {
        when(repository.existsByShortCode("products")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com/products", "products", null);

        assertThat(response.shortCode()).isEqualTo("products");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/products");
        verify(generator, never()).generate();
        verify(repository, times(1)).save(argThatShortCodeIs("products"));
    }

    @Test
    void createShortUrl_customAliasAlreadyExists_throwsConflict_doesNotRetryWithRandomCode() {
        when(repository.existsByShortCode("products")).thenReturn(true);

        assertThatThrownBy(() -> service.createShortUrl("https://example.com/products", "products", null))
                .isInstanceOf(ShortCodeAlreadyExistsException.class);

        // Must not silently fall back to a generated code — the caller asked for this exact alias.
        verify(generator, never()).generate();
        verify(repository, never()).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_customAliasRaceLostToDatabase_throwsConflict_doesNotRetry() {
        // Pre-check passes (simulating a race lost between check and insert); the database's
        // UNIQUE constraint is what actually catches it.
        when(repository.existsByShortCode("products")).thenReturn(false);
        when(repository.save(argThatShortCodeIs("products")))
                .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        assertThatThrownBy(() -> service.createShortUrl("https://example.com/products", "products", null))
                .isInstanceOf(ShortCodeAlreadyExistsException.class);

        // Exactly one attempt — unlike the generated-code path, a colliding alias is never retried.
        verify(repository, times(1)).save(any(UrlMapping.class));
    }

    // --- createShortUrl with expiresAt (ambiguous-requirement scenario — see
    // docs/AI_WORKLOG.md, "Shortened URLs should expire") ---

    @Test
    void createShortUrl_withExpiresAt_persistsAndReturnsIt() {
        Instant expiry = FIXED_NOW.plusSeconds(3600);
        when(generator.generate()).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com", null, expiry);

        assertThat(response.expiresAt()).isEqualTo(expiry);
        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void createShortUrl_withoutExpiresAt_responseExpiresAtIsNull() {
        when(generator.generate()).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com", null, null);

        assertThat(response.expiresAt()).isNull();
    }

    @Test
    void createShortUrl_withCustomAliasAndExpiresAt_bothApply() {
        Instant expiry = FIXED_NOW.plusSeconds(3600);
        when(repository.existsByShortCode("products")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com/products", "products", expiry);

        assertThat(response.shortCode()).isEqualTo("products");
        assertThat(response.expiresAt()).isEqualTo(expiry);
    }

    // --- resolveAndRecordRedirect ---

    @Test
    void resolveAndRecordRedirect_returnsOriginalUrlAndIncrementsCount() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "found01");
        when(repository.findByShortCode("found01")).thenReturn(Optional.of(mapping));

        String originalUrl = service.resolveAndRecordRedirect("found01");

        assertThat(originalUrl).isEqualTo("https://example.com/target");
        verify(repository, times(1)).incrementClickCount("found01");
    }

    @Test
    void resolveAndRecordRedirect_unknownShortCode_throwsAndDoesNotIncrement() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveAndRecordRedirect("missing"))
                .isInstanceOf(ShortCodeNotFoundException.class);

        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    void resolveAndRecordRedirect_nonExpiredMapping_succeeds() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "found01", FIXED_NOW.plusSeconds(1));
        when(repository.findByShortCode("found01")).thenReturn(Optional.of(mapping));

        String originalUrl = service.resolveAndRecordRedirect("found01");

        assertThat(originalUrl).isEqualTo("https://example.com/target");
        verify(repository, times(1)).incrementClickCount("found01");
    }

    @Test
    void resolveAndRecordRedirect_expiredMapping_throwsSameExceptionAsNotFound_andDoesNotIncrement() {
        // At or after expiresAt counts as expired — FIXED_NOW is exactly the expiry instant here.
        UrlMapping mapping = new UrlMapping("https://example.com/target", "expired1", FIXED_NOW);
        when(repository.findByShortCode("expired1")).thenReturn(Optional.of(mapping));

        assertThatThrownBy(() -> service.resolveAndRecordRedirect("expired1"))
                .isInstanceOf(ShortCodeNotFoundException.class);

        // Must not increment a click against an expired link.
        verify(repository, never()).incrementClickCount(any());
    }

    // --- getUrlDetails ---

    @Test
    void getUrlDetails_returnsMetadataWithoutRedirectingOrIncrementing() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "found01");
        when(repository.findByShortCode("found01")).thenReturn(Optional.of(mapping));

        UrlDetailsResponse response = service.getUrlDetails("found01");

        assertThat(response.originalUrl()).isEqualTo("https://example.com/target");
        assertThat(response.shortCode()).isEqualTo("found01");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/found01");
        assertThat(response.createdAt()).isEqualTo(mapping.getCreatedAt());
        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    void getUrlDetails_unknownShortCode_throws() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUrlDetails("missing"))
                .isInstanceOf(ShortCodeNotFoundException.class);

        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    void getUrlDetails_nonExpiredMapping_succeeds() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "found01", FIXED_NOW.plusSeconds(1));
        when(repository.findByShortCode("found01")).thenReturn(Optional.of(mapping));

        assertThat(service.getUrlDetails("found01").originalUrl()).isEqualTo("https://example.com/target");
    }

    @Test
    void getUrlDetails_expiredMapping_throwsSameExceptionAsNotFound() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "expired1", FIXED_NOW.minusSeconds(1));
        when(repository.findByShortCode("expired1")).thenReturn(Optional.of(mapping));

        assertThatThrownBy(() -> service.getUrlDetails("expired1"))
                .isInstanceOf(ShortCodeNotFoundException.class);
    }

    // --- getUrlAnalytics ---
    // clickCount is only ever mutated via the atomic UPDATE (UrlMapping has no setter/increment
    // method); real click-accumulation is covered at the repository/integration level where
    // that atomic increment actually runs. This unit test covers call-forwarding and the
    // read-only guarantee, which is all that's expressible against a mocked repository.

    @Test
    void getUrlAnalytics_returnsAllFieldsWithoutIncrementing() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "found01");
        when(repository.findByShortCode("found01")).thenReturn(Optional.of(mapping));

        UrlAnalyticsResponse response = service.getUrlAnalytics("found01");

        assertThat(response.shortCode()).isEqualTo("found01");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/found01");
        assertThat(response.originalUrl()).isEqualTo("https://example.com/target");
        assertThat(response.clickCount()).isEqualTo(mapping.getClickCount());
        assertThat(response.createdAt()).isEqualTo(mapping.getCreatedAt());
        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    void getUrlAnalytics_unknownShortCode_throws() {
        when(repository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUrlAnalytics("missing"))
                .isInstanceOf(ShortCodeNotFoundException.class);

        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    void getUrlAnalytics_nonExpiredMapping_succeeds() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "found01", FIXED_NOW.plusSeconds(1));
        when(repository.findByShortCode("found01")).thenReturn(Optional.of(mapping));

        assertThat(service.getUrlAnalytics("found01").shortCode()).isEqualTo("found01");
    }

    @Test
    void getUrlAnalytics_expiredMapping_throwsSameExceptionAsNotFound() {
        UrlMapping mapping = new UrlMapping("https://example.com/target", "expired1", FIXED_NOW.minusSeconds(1));
        when(repository.findByShortCode("expired1")).thenReturn(Optional.of(mapping));

        assertThatThrownBy(() -> service.getUrlAnalytics("expired1"))
                .isInstanceOf(ShortCodeNotFoundException.class);
    }
}
