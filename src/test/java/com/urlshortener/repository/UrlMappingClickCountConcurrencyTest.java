package com.urlshortener.repository;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstrates that concurrent redirects don't lose click-count updates — the property the
 * atomic {@code UPDATE ... SET x = x + 1} approach
 * ({@link UrlMappingRepository#incrementClickCount}) exists to guarantee, as opposed to a
 * read-then-write-in-Java approach.
 *
 * <p>Goes through {@link UrlService#resolveAndRecordRedirect}, not the repository directly:
 * that method's own {@code @Transactional} is what provides each call its transaction (Spring
 * Data's {@code @Modifying} queries require an active one — confirmed the hard way while
 * writing this test: calling the repository directly with no surrounding transaction threw
 * {@code TransactionRequiredException}, not a silent no-op). Exercising the real service method
 * is also more representative of what actually happens under concurrent redirect traffic.
 *
 * <p>Deliberately <b>not</b> {@code @DataJpaTest}: that annotation wraps each test method in a
 * single transaction bound to the JUnit-executing thread, which would prevent worker threads
 * spawned here from seeing each other's writes (Spring's transaction binding is thread-local).
 * A plain {@code @SpringBootTest} with no test-level {@code @Transactional} lets each worker
 * thread's call run in its own independent, committing transaction — the same way genuinely
 * concurrent requests would behave in production.
 */
@SpringBootTest
class UrlMappingClickCountConcurrencyTest {

    @Autowired
    private UrlMappingRepository repository;

    @Autowired
    private UrlService urlService;

    @Test
    void concurrentRedirects_doNotLoseClickCountUpdates() throws InterruptedException {
        UrlMapping saved = repository.saveAndFlush(new UrlMapping("https://example.com", "concur1"));
        String shortCode = saved.getShortCode();

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch allReady = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                allReady.countDown();
                go.await(); // release all threads together to maximize actual overlap
                urlService.resolveAndRecordRedirect(shortCode);
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }
        allReady.await();
        go.countDown();

        // .get() on every future — submit()-and-forget would silently swallow any exception a
        // worker threw, making the test pass for the wrong reason instead of failing loudly.
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new AssertionError("A concurrent redirect call failed", e.getCause());
            }
        }
        executor.shutdown();

        assertThat(repository.findByShortCode(shortCode).orElseThrow().getClickCount())
                .as("every one of %d concurrent redirects must be reflected — none lost", threadCount)
                .isEqualTo(threadCount);
    }
}
