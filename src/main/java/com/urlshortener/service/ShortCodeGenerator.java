package com.urlshortener.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates random 7-character Base62 short codes. Approved strategy (see
 * docs/AI_WORKLOG.md, "Decision: Short-code generation strategy"): {@link SecureRandom}, not
 * {@link java.util.Random}; no sequential/database-derived IDs. Collision avoidance is this
 * class's job only in the statistical sense (62^7 ≈ 3.5 trillion codes) — actual uniqueness is
 * enforced by the database's {@code UNIQUE} constraint and the caller's retry loop, not here.
 * Deliberately has no Spring/persistence dependencies so it's trivially unit-testable.
 */
@Component
public class ShortCodeGenerator {

    static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final int CODE_LENGTH = 7;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
