package com.urlshortener.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

/**
 * Validates that a string is a syntactically valid URL with scheme {@code http} or
 * {@code https} and a non-empty host. Deliberately does not attempt any deeper safety checks
 * (e.g. blocking private/loopback hosts) — that's a separate, not-yet-decided concern (see
 * docs/AI_WORKLOG.md), not something this constraint silently takes on.
 */
public class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            // Presence is @NotBlank's job; an absent value is not this constraint's concern.
            return true;
        }
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
                return false;
            }
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
