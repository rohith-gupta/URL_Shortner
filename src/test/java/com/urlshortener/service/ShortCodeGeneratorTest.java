package com.urlshortener.service;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @RepeatedTest(20)
    void generatesExactlySevenCharacters() {
        assertThat(generator.generate()).hasSize(7);
    }

    @RepeatedTest(20)
    void generatesOnlyBase62Characters() {
        assertThat(generator.generate()).matches("[0-9a-zA-Z]{7}");
    }

    @Test
    void generatesDistinctCodesAcrossManyCalls() {
        // Statistical, not exact: with a 62^7 (~3.5 trillion) code space, collisions among 1000
        // samples are astronomically unlikely unless the generator is broken (e.g. always
        // returning the same value, or seeded predictably). A high but not-quite-100% threshold
        // keeps this non-flaky while still catching a genuinely broken generator.
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate());
        }
        assertThat(codes).hasSizeGreaterThan(990);
    }

    @Test
    void alphabetIsExactlyBase62() {
        assertThat(ShortCodeGenerator.ALPHABET).hasSize(62);
        assertThat(ShortCodeGenerator.ALPHABET).matches("[0-9a-zA-Z]{62}");
    }

    // --- ROUTE_PATTERN (shared by RedirectController and UrlController's details/analytics
    // routes; must accept both generated codes and custom aliases — see docs/AI_WORKLOG.md,
    // "Brownfield: add optional custom aliases to POST /api/urls") ---

    @Test
    void routePattern_acceptsAnyGeneratedCode() {
        assertThat(generator.generate()).matches(ShortCodeGenerator.ROUTE_PATTERN);
    }

    @Test
    void routePattern_acceptsValidCustomAliases() {
        assertThat("products").matches(ShortCodeGenerator.ROUTE_PATTERN);
        assertThat("my-page_1").matches(ShortCodeGenerator.ROUTE_PATTERN);
        assertThat("abcd").matches(ShortCodeGenerator.ROUTE_PATTERN); // exactly 4, the minimum
        assertThat("a".repeat(30)).matches(ShortCodeGenerator.ROUTE_PATTERN); // exactly 30, the maximum
    }

    @Test
    void routePattern_rejectsOutOfRangeOrInvalidCharacters() {
        assertThat("abc").doesNotMatch(ShortCodeGenerator.ROUTE_PATTERN); // 3 chars, below minimum
        assertThat("a".repeat(31)).doesNotMatch(ShortCodeGenerator.ROUTE_PATTERN); // 31 chars, above maximum
        assertThat("abc.de").doesNotMatch(ShortCodeGenerator.ROUTE_PATTERN); // '.' not allowed
        assertThat("has space").doesNotMatch(ShortCodeGenerator.ROUTE_PATTERN);
    }
}
