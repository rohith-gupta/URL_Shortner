package com.urlshortener.repository;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates that concurrent requests for the <em>same</em> custom alias can't both "win" —
 * the property {@link UrlService#createShortUrl} claims for the custom-alias path (a colliding
 * alias returns {@code 409}, never a retry with a different code; see that method's Javadoc and
 * docs/AI_WORKLOG.md's brownfield entry), but which, before this test, was only ever proven
 * against a mocked repository simulating a single race
 * ({@code UrlServiceTest#createShortUrl_customAliasRaceLostToDatabase_throwsConflict_doesNotRetry}),
 * never against real concurrent writes hitting the real {@code UNIQUE} constraint.
 *
 * <p>This is the direct implementation of Gap 2 from docs/AI_WORKLOG.md's "Test-improvement
 * scenario (FR-7): gap analysis" entry — that entry's rationale explains why this specific gap
 * was recommended (this project's own click-count concurrency test previously "passed for the
 * wrong reason" until fixed to genuinely wait on every thread, at which point it surfaced a real
 * bug; the same class of hidden risk was never ruled out for alias collisions). This test is
 * structurally modeled on {@link UrlMappingClickCountConcurrencyTest} for that reason.
 *
 * <p>Deliberately <b>not</b> {@code @DataJpaTest}, for the same reason documented on
 * {@link UrlMappingClickCountConcurrencyTest}: that annotation binds each test method to a
 * single transaction on the JUnit-executing thread, which would prevent worker threads spawned
 * here from racing against each other and the real database at all.
 */
@SpringBootTest
class UrlAliasCollisionConcurrencyTest {

    @Autowired
    private UrlMappingRepository repository;

    @Autowired
    private UrlService urlService;

    @Test
    void concurrentCreatesForSameAlias_exactlyOneWins_restAreRejectedWithConflict() throws InterruptedException {
        String alias = "raceAlias";
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch allReady = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);

        List<Callable<Optional<CreateUrlResponse>>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            String url = "https://example.com/racer-" + i;
            tasks.add(() -> {
                allReady.countDown();
                go.await(); // release all threads together to maximize actual overlap
                try {
                    return Optional.of(urlService.createShortUrl(url, alias, null));
                } catch (ShortCodeAlreadyExistsException expectedForEveryLoser) {
                    // The one guarantee under test: losing the race must look like this, not a
                    // silently-substituted different short code and not a generic failure.
                    return Optional.empty();
                }
            });
        }

        List<Future<Optional<CreateUrlResponse>>> futures = new ArrayList<>();
        for (Callable<Optional<CreateUrlResponse>> task : tasks) {
            futures.add(executor.submit(task));
        }
        allReady.await();
        go.countDown();

        // .get() on every future — submit()-and-forget would silently swallow any *unexpected*
        // exception a worker threw, making the test pass for the wrong reason instead of failing
        // loudly (the exact mistake this class exists to avoid repeating — see class Javadoc).
        // ShortCodeAlreadyExistsException is already handled above; anything else surfaces here.
        List<CreateUrlResponse> winners = new ArrayList<>();
        for (Future<Optional<CreateUrlResponse>> future : futures) {
            try {
                future.get().ifPresent(winners::add);
            } catch (ExecutionException e) {
                throw new AssertionError("A concurrent createShortUrl call failed unexpectedly", e.getCause());
            }
        }
        executor.shutdown();

        assertThat(winners)
                .as("exactly one of %d concurrent requests for the same alias must succeed", threadCount)
                .hasSize(1);
        assertThat(winners.get(0).shortCode()).isEqualTo(alias);

        // The UNIQUE constraint makes a duplicate row physically impossible, but this confirms
        // the winning row is actually there under the alias, not lost or corrupted by the race.
        assertThat(repository.findByShortCode(alias))
                .as("the database must end up with exactly one row for the alias")
                .isPresent()
                .get().extracting(com.urlshortener.entity.UrlMapping::getOriginalUrl)
                .isEqualTo(winners.get(0).originalUrl());
    }
}
