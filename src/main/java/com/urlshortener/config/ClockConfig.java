package com.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Exposes the "current time" as an injectable {@link Clock} bean rather than code calling
 * {@code Instant.now()} directly, so time-dependent logic (expiration checks — see
 * {@code UrlService}) can be tested deterministically with a fixed clock instead of real,
 * wall-clock-dependent (and therefore potentially flaky or sleep-requiring) timing.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
