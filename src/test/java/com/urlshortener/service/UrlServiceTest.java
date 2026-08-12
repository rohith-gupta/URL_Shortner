package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlDetailsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

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

    private UrlMappingRepository repository;
    private ShortCodeGenerator generator;
    private UrlService service;

    @BeforeEach
    void setUp() {
        repository = mock(UrlMappingRepository.class);
        generator = mock(ShortCodeGenerator.class);
        service = new UrlService(repository, generator, "http://localhost:8080");
        // Default: repository.save just echoes back whatever entity it was given.
        when(repository.save(any(UrlMapping.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsShortUrlOnFirstAttempt_whenNoCollision() {
        when(generator.generate()).thenReturn("abc1234");
        when(repository.existsByShortCode("abc1234")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com");

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

        service.createShortUrl("  https://example.com  ");

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOriginalUrl()).isEqualTo("https://example.com");
    }

    @Test
    void retriesWithNewCodeWhenPreCheckFindsExistingCode() {
        when(generator.generate()).thenReturn("collide", "fresh01");
        when(repository.existsByShortCode("collide")).thenReturn(true);
        when(repository.existsByShortCode("fresh01")).thenReturn(false);

        CreateUrlResponse response = service.createShortUrl("https://example.com");

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

        CreateUrlResponse response = service.createShortUrl("https://example.com");

        assertThat(response.shortCode()).isEqualTo("afterRc");
        verify(repository, times(2)).save(any(UrlMapping.class));
    }

    @Test
    void throwsAfterExhaustingBoundedRetries() {
        when(generator.generate()).thenReturn("aaaaaaa");
        when(repository.existsByShortCode("aaaaaaa")).thenReturn(true); // always collides

        assertThatThrownBy(() -> service.createShortUrl("https://example.com"))
                .isInstanceOf(ShortCodeGenerationException.class);

        // Bounded: exactly 5 attempts, not unbounded looping.
        verify(generator, times(5)).generate();
        verify(repository, times(0)).save(any(UrlMapping.class));
    }

    private static UrlMapping argThatShortCodeIs(String shortCode) {
        return org.mockito.ArgumentMatchers.argThat(m -> m != null && shortCode.equals(m.getShortCode()));
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
}
