package com.urlshortener.dto.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HttpUrlValidatorTest {

    private final HttpUrlValidator validator = new HttpUrlValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.com",
            "https://example.com",
            "https://example.com/some/path?query=1&x=2",
            "HTTPS://Example.com", // scheme is case-insensitive
            "http://sub.example.com:8080/path"
    })
    void acceptsValidHttpAndHttpsUrls(String value) {
        assertThat(validator.isValid(value, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ftp://example.com",
            "javascript:alert(1)",
            "mailto:someone@example.com",
            "example.com",          // no scheme
            "http://",              // no host
            "not a url at all",
            "http:///nohost"
    })
    void rejectsNonHttpSchemesAndMalformedUrls(String value) {
        assertThat(validator.isValid(value, null)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void treatsBlankOrNullAsValid_leavesPresenceToNotBlank(String value) {
        assertThat(validator.isValid(value, null)).isTrue();
    }
}
