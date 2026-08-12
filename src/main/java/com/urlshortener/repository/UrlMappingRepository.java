package com.urlshortener.repository;

import com.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Fast pre-check used before inserting a newly generated short code. Not sufficient on its
     * own to prevent collisions under concurrency — the database's {@code UNIQUE} constraint on
     * {@code short_code} is the actual guarantee; this is only an optimization to usually avoid
     * a doomed insert.
     */
    boolean existsByShortCode(String shortCode);

    /** Resolves a short code to its mapping for the redirect endpoint. */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /**
     * Atomically increments the click count for a short code with a single
     * {@code UPDATE ... SET click_count = click_count + 1} statement — the increment happens
     * entirely in the database, so concurrent redirects for the same code can't lose updates
     * the way a read-in-Java-then-write-back approach could. Returns the number of rows
     * affected (0 or 1); callers that already confirmed the row exists via
     * {@link #findByShortCode} don't need to check it.
     *
     * <p>{@code clearAutomatically = true}: a bulk {@code @Modifying} update bypasses
     * Hibernate's first-level cache, so without this, an entity already loaded earlier in the
     * same persistence context (e.g. by {@link #findByShortCode} just before this call) would
     * keep showing its stale in-memory {@code clickCount} on a subsequent read within the same
     * transaction, even though the row is correctly updated in the database.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    int incrementClickCount(@Param("shortCode") String shortCode);
}
