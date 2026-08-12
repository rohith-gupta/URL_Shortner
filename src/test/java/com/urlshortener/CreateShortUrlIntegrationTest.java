package com.urlshortener;

import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test: real controller, real service, real repository, real (H2, test-scope)
 * database — exercised through Spring MVC's actual dispatcher via {@link MockMvc}, not mocked
 * at any layer below the HTTP boundary. This is what actually proves the entity mapping is
 * consistent with the schema Hibernate generates for it (an equivalent check to what
 * ddl-auto=validate performs against the real Flyway-managed PostgreSQL schema at runtime).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreateShortUrlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void createShortUrl_withValidUrl_persistsAndReturns201() throws Exception {
        long before = repository.count();

        String responseBody = mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/some/path\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/some/path"))
                .andExpect(jsonPath("$.shortCode").exists())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn().getResponse().getContentAsString();

        String shortCode = com.jayway.jsonpath.JsonPath.read(responseBody, "$.shortCode");
        assertThat(shortCode).matches("[0-9a-zA-Z]{7}");
        assertThat(repository.count()).isEqualTo(before + 1);
        assertThat(repository.existsByShortCode(shortCode)).isTrue();
    }

    @Test
    void createShortUrl_withBlankUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createShortUrl_withUnsupportedScheme_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"ftp://example.com/file\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShortUrl_withMalformedUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"not a url\"}"))
                .andExpect(status().isBadRequest());
    }
}
