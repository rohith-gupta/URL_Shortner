package com.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;

/**
 * A persisted mapping from a short code — either randomly generated or a custom alias, both
 * live in this same {@code shortCode} column, see docs/AI_WORKLOG.md "Brownfield: add optional
 * custom aliases" — to the original long URL. Schema is owned by Flyway
 * ({@code src/main/resources/db/migration}); this mapping is validated against that schema at
 * startup (Hibernate {@code ddl-auto=validate}), not used to generate it.
 */
@Entity
@Table(
        name = "url_mapping",
        uniqueConstraints = @UniqueConstraint(name = "uq_url_mapping_short_code", columnNames = "short_code")
)
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "original_url", length = 2048, nullable = false)
    private String originalUrl;

    @Column(name = "short_code", length = 30, nullable = false, unique = true)
    private String shortCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "click_count", nullable = false)
    private long clickCount;

    /** Required by JPA; not for application use. */
    protected UrlMapping() {
    }

    public UrlMapping(String originalUrl, String shortCode) {
        this.originalUrl = Objects.requireNonNull(originalUrl, "originalUrl must not be null");
        this.shortCode = Objects.requireNonNull(shortCode, "shortCode must not be null");
        this.createdAt = Instant.now();
        this.clickCount = 0L;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getClickCount() {
        return clickCount;
    }
}
