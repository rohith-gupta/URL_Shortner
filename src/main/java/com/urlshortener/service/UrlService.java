package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.dto.UrlDetailsResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for creating shortened URLs.
 *
 * <p><b>Deliberately not {@code @Transactional}.</b> Each {@link UrlMappingRepository} call
 * (save/existsByShortCode) already runs in its own repository-managed transaction. If this
 * method were wrapped in a single transaction spanning multiple generation attempts, a unique
 * constraint violation from PostgreSQL would mark that whole transaction rollback-only ("current
 * transaction is aborted, commands ignored until end of transaction block"), breaking retry —
 * the second attempt would fail even with a fresh, non-colliding code. Letting each attempt use
 * its own transaction keeps a collision on attempt N from poisoning attempt N+1.
 */
@Service
public class UrlService {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);

    /**
     * Small, bounded retry limit for short-code collisions. 62^7 (~3.5 trillion) codes means a
     * real collision is already vanishingly unlikely; this bound exists only to fail fast and
     * loudly instead of looping if something is systemically wrong, per the approved strategy
     * (see docs/AI_WORKLOG.md, "Decision: Short-code generation strategy").
     */
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final UrlMappingRepository repository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String baseUrl;

    public UrlService(UrlMappingRepository repository,
                       ShortCodeGenerator shortCodeGenerator,
                       @Value("${app.base-url}") String baseUrl) {
        this.repository = repository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.baseUrl = baseUrl;
    }

    public CreateUrlResponse createShortUrl(String originalUrl) {
        String normalizedUrl = originalUrl.trim();

        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = shortCodeGenerator.generate();

            // Fast pre-check to usually avoid a doomed insert; NOT the actual guarantee.
            if (repository.existsByShortCode(candidate)) {
                log.warn("Short code collision (pre-check) on attempt {}/{}", attempt, MAX_GENERATION_ATTEMPTS);
                continue;
            }

            try {
                UrlMapping saved = repository.save(new UrlMapping(normalizedUrl, candidate));
                return toResponse(saved);
            } catch (DataIntegrityViolationException raceLost) {
                // Two requests generated the same code between our pre-check and the insert;
                // the database's UNIQUE constraint is what actually caught it. Retry with a
                // fresh code in a new attempt/transaction.
                log.warn("Short code collision (unique constraint) on attempt {}/{}", attempt, MAX_GENERATION_ATTEMPTS);
            }
        }

        throw new ShortCodeGenerationException(
                "Unable to generate a unique short code after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private CreateUrlResponse toResponse(UrlMapping mapping) {
        return new CreateUrlResponse(mapping.getOriginalUrl(), mapping.getShortCode(), buildShortUrl(mapping), mapping.getCreatedAt());
    }

    private String buildShortUrl(UrlMapping mapping) {
        return baseUrl + "/" + mapping.getShortCode();
    }

    /**
     * Resolves a short code to its original URL and records a redirect (click) against it.
     *
     * <p>{@code @Transactional} here — unlike {@link #createShortUrl}, which deliberately
     * avoids it. There's no bounded-retry loop to protect here, so there's no risk of one
     * attempt's failure poisoning a later one; the transaction exists because Spring Data JPA
     * requires an active transaction to run a {@code @Modifying} query
     * ({@link UrlMappingRepository#incrementClickCount}), and grouping the lookup with the
     * increment into one short-lived transaction is simply the cleanest boundary for "these two
     * things happen together" — it is <em>not</em> what makes the counter itself safe under
     * concurrency. That safety comes from the increment being a single atomic
     * {@code UPDATE ... SET click_count = click_count + 1} statement in the database, not from
     * this method's transactional boundary.
     *
     * @throws ShortCodeNotFoundException if no mapping exists for {@code shortCode}
     */
    @Transactional
    public String resolveAndRecordRedirect(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        repository.incrementClickCount(shortCode);
        return mapping.getOriginalUrl();
    }

    /**
     * Looks up short URL metadata without redirecting, incrementing the click count, or
     * otherwise modifying anything — a pure read. {@code @Transactional(readOnly = true)}: not
     * required for correctness (a single {@code findByShortCode} needs no transaction at all
     * for the same reason {@link #createShortUrl}'s reads don't), but it lets Hibernate skip
     * dirty-checking for this call and documents the intent unambiguously — this method must
     * never gain a write in the future without that becoming visually obvious.
     *
     * @throws ShortCodeNotFoundException if no mapping exists for {@code shortCode}
     */
    @Transactional(readOnly = true)
    public UrlDetailsResponse getUrlDetails(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        return new UrlDetailsResponse(mapping.getOriginalUrl(), mapping.getShortCode(), buildShortUrl(mapping), mapping.getCreatedAt());
    }

    /**
     * Looks up basic click-count analytics for a short code — {@code clickCount} as already
     * maintained on {@link UrlMapping} by {@link #resolveAndRecordRedirect}, nothing more. A
     * pure read, same shape and same rationale as {@link #getUrlDetails}: no write here, so
     * {@code @Transactional(readOnly = true)} rather than a fresh justification. Deliberately
     * does not compute or store anything beyond the existing counter — richer analytics
     * (referrer/device/geography) is explicitly out of scope for this endpoint, not a gap to
     * quietly fill in later without a decision.
     *
     * @throws ShortCodeNotFoundException if no mapping exists for {@code shortCode}
     */
    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getUrlAnalytics(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        return new UrlAnalyticsResponse(mapping.getShortCode(), buildShortUrl(mapping), mapping.getOriginalUrl(),
                mapping.getClickCount(), mapping.getCreatedAt());
    }
}
