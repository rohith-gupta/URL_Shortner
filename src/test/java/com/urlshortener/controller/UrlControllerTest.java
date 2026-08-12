package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.dto.UrlAnalyticsResponse;
import com.urlshortener.dto.UrlDetailsResponse;
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
        when(urlService.createShortUrl("https://example.com")).thenReturn(stubbed);

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
        mockMvc.perform(get("/api/urls/short")).andExpect(status().isNotFound());
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
        mockMvc.perform(get("/api/urls/short/analytics")).andExpect(status().isNotFound());
    }
}
