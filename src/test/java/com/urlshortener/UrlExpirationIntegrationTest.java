package com.urlshortener;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests for optional URL expiration (ambiguous-requirement scenario — see
 * docs/AI_WORKLOG.md, "Shortened URLs should expire"). Real controller, real service, real
 * repository, real (H2, test-scope) database. Expired mappings are constructed directly via
 * the repository (a past {@code expiresAt} could never pass creation's {@code @Future}
 * validation — the only way to get one is either an already-valid link outliving its
 * expiration, or, for the test's purposes, saving one directly) rather than by waiting for
 * real time to pass, per the "avoid sleep-based tests" instruction.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlExpirationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void createWithFutureExpiresAt_persists_andWorksAcrossRedirectDetailsAnalytics() throws Exception {
        String expiresAt = "2099-01-01T00:00:00Z";

        String body = mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/expiring\",\"expiresAt\":\"" + expiresAt + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiresAt").value(expiresAt))
                .andReturn().getResponse().getContentAsString();

        String shortCode = com.jayway.jsonpath.JsonPath.read(body, "$.shortCode");
        assertThat(repository.findByShortCode(shortCode).orElseThrow().getExpiresAt())
                .isEqualTo(Instant.parse(expiresAt));

        mockMvc.perform(get("/" + shortCode)).andExpect(status().isFound());
        mockMvc.perform(get("/api/urls/" + shortCode)).andExpect(status().isOk());
        mockMvc.perform(get("/api/urls/" + shortCode + "/analytics")).andExpect(status().isOk());
    }

    @Test
    void createWithPastExpiresAt_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"expiresAt\":\"2020-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expiresAt"));
    }

    @Test
    void createWithCurrentExpiresAt_returns400() throws Exception {
        String now = Instant.now().toString();
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"expiresAt\":\"" + now + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("expiresAt"));
    }

    @Test
    void createWithCustomAliasAndExpiresAt_bothApply() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/promo\","
                                + "\"customAlias\":\"promo2026\",\"expiresAt\":\"2099-01-01T00:00:00Z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("promo2026"))
                .andExpect(jsonPath("$.expiresAt").value("2099-01-01T00:00:00Z"));

        mockMvc.perform(get("/promo2026")).andExpect(status().isFound());
    }

    @Test
    void createWithoutExpiresAt_neverExpires_existingBehaviorUnchanged() throws Exception {
        String body = mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/permanent\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expiresAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String shortCode = com.jayway.jsonpath.JsonPath.read(body, "$.shortCode");
        assertThat(shortCode).matches("[0-9a-zA-Z]{7}");
        assertThat(repository.findByShortCode(shortCode).orElseThrow().getExpiresAt()).isNull();

        mockMvc.perform(get("/" + shortCode)).andExpect(status().isFound());
    }

    @Test
    void expiredMapping_redirectDetailsAndAnalyticsAllReturn404() throws Exception {
        UrlMapping saved = repository.saveAndFlush(
                new UrlMapping("https://example.com/gone", "expird1", Instant.now().minusSeconds(3600)));

        mockMvc.perform(get("/" + saved.getShortCode()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/api/urls/" + saved.getShortCode()))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/urls/" + saved.getShortCode() + "/analytics"))
                .andExpect(status().isNotFound());

        // Must not have been counted as a click.
        assertThat(repository.findByShortCode("expird1").orElseThrow().getClickCount()).isZero();
    }

    @Test
    void expiredMapping_redirect404_isIdenticalInShapeToUnknownCode404() throws Exception {
        repository.saveAndFlush(new UrlMapping("https://example.com/gone", "expird2", Instant.now().minusSeconds(3600)));

        String expiredBody = mockMvc.perform(get("/expird2"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        String unknownBody = mockMvc.perform(get("/zzzzzzz"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        // Same message shape for both (modulo the differing code in the message) — no
        // distinguishing signal between "expired" and "never existed", by design.
        assertThat(com.jayway.jsonpath.JsonPath.<String>read(expiredBody, "$.error"))
                .isEqualTo(com.jayway.jsonpath.JsonPath.read(unknownBody, "$.error"));
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(expiredBody, "$.status"))
                .isEqualTo(com.jayway.jsonpath.JsonPath.read(unknownBody, "$.status"));
    }

    @Test
    void nullExpiresAtMapping_neverExpires_worksRegardlessOfTime() throws Exception {
        UrlMapping saved = repository.saveAndFlush(new UrlMapping("https://example.com/forever", "forevr1"));
        assertThat(saved.getExpiresAt()).isNull();

        mockMvc.perform(get("/forevr1")).andExpect(status().isFound());
        mockMvc.perform(get("/api/urls/forevr1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/urls/forevr1/analytics")).andExpect(status().isOk());
    }

    @Test
    void existingCreateEndpoint_stillWorksAlongsideExpiration() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/still-works\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.shortCode").exists());
    }
}
