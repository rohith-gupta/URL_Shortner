package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.dto.UrlDetailsResponse;
import com.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-slice tests: HTTP contract only (status, headers, body shape, validation
 * wiring). {@link UrlService} is mocked — business logic itself is covered by
 * {@link com.urlshortener.service.UrlServiceTest} and the full-stack integration test.
 */
@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void createShortUrl_returns201WithLocationAndBody() throws Exception {
        CreateUrlResponse stubbed = new CreateUrlResponse(
                "https://example.com", "abc1234", "http://localhost:8080/abc1234", Instant.parse("2026-08-12T10:00:00Z"));
        when(urlService.createShortUrl("https://example.com", null)).thenReturn(stubbed);

        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/urls/abc1234")))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"));
    }

    // --- customAlias (brownfield enhancement — see docs/AI_WORKLOG.md) ---

    @Test
    void createShortUrl_withValidCustomAlias_returns201WithAliasAsShortCode() throws Exception {
        CreateUrlResponse stubbed = new CreateUrlResponse(
                "https://example.com/products", "products", "http://localhost:8080/products", Instant.parse("2026-08-12T10:00:00Z"));
        when(urlService.createShortUrl("https://example.com/products", "products")).thenReturn(stubbed);

        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/products\",\"customAlias\":\"products\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/urls/products")))
                .andExpect(jsonPath("$.shortCode").value("products"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/products"));
    }

    @Test
    void createShortUrl_duplicateCustomAlias_returns409() throws Exception {
        when(urlService.createShortUrl("https://example.com/products", "products"))
                .thenThrow(new ShortCodeAlreadyExistsException("products"));

        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/products\",\"customAlias\":\"products\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void createShortUrl_customAliasTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void createShortUrl_customAliasTooLong_returns400() throws Exception {
        String tooLong = "a".repeat(31);
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void createShortUrl_customAliasWithSpace_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"has space\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void createShortUrl_customAliasWithSlash_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"has/slash\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void createShortUrl_customAliasWithUnsafeCharacters_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"abc?d=1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void createShortUrl_blankOriginalUrl_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("originalUrl"));
    }

    @Test
    void createShortUrl_unsupportedScheme_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"ftp://example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("originalUrl"));
    }

    @Test
    void createShortUrl_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createShortUrl_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/urls").contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUrlDetails_existingShortCode_returns200WithBody() throws Exception {
        UrlDetailsResponse stubbed = new UrlDetailsResponse(
                "https://example.com", "abc1234", "http://localhost:8080/abc1234", Instant.parse("2026-08-12T10:00:00Z"));
        when(urlService.getUrlDetails("abc1234")).thenReturn(stubbed);

        mockMvc.perform(get("/api/urls/abc1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.clickCount").doesNotExist());
    }

    @Test
    void getUrlDetails_unknownShortCode_returns404() throws Exception {
        when(urlService.getUrlDetails("zzzzzzz")).thenThrow(new ShortCodeNotFoundException("zzzzzzz"));

        mockMvc.perform(get("/api/urls/zzzzzzz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getUrlDetails_malformedShortCode_returns404() throws Exception {
        // 3 chars — below the new 4-char alias minimum (route widened for custom aliases;
        // see docs/AI_WORKLOG.md, "Brownfield: add optional custom aliases"). "short" (5
        // chars) used to be malformed under the old fixed-7 rule but no longer is.
        mockMvc.perform(get("/api/urls/abc")).andExpect(status().isNotFound());
    }

    @Test
    void getUrlAnalytics_existingShortCode_returns200WithBody() throws Exception {
        UrlAnalyticsResponse stubbed = new UrlAnalyticsResponse(
                "abc1234", "http://localhost:8080/abc1234", "https://example.com", 42L, Instant.parse("2026-08-12T10:00:00Z"));
        when(urlService.getUrlAnalytics("abc1234")).thenReturn(stubbed);

        mockMvc.perform(get("/api/urls/abc1234/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.clickCount").value(42))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getUrlAnalytics_unknownShortCode_returns404() throws Exception {
        when(urlService.getUrlAnalytics("zzzzzzz")).thenThrow(new ShortCodeNotFoundException("zzzzzzz"));

        mockMvc.perform(get("/api/urls/zzzzzzz/analytics"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getUrlAnalytics_malformedShortCode_returns404() throws Exception {
        mockMvc.perform(get("/api/urls/abc/analytics")).andExpect(status().isNotFound());
    }
}
