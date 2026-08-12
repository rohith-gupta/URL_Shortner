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
}
