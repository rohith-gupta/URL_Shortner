package com.urlshortener;

import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests for the brownfield custom-alias enhancement to {@code POST /api/urls} (see
 * docs/AI_WORKLOG.md, "Brownfield: add optional custom aliases"). Real controller, real
 * service, real repository, real (H2, test-scope) database — nothing mocked. The pre-existing
 * {@code CreateShortUrlIntegrationTest}, {@code RedirectIntegrationTest},
 * {@code UrlDetailsIntegrationTest}, and {@code UrlAnalyticsIntegrationTest} are untouched
 * (beyond fixing the malformed-short-code examples they used — see their own comments) and
 * still pass, which is the real proof that this enhancement is backward compatible.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CustomAliasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void createWithoutAlias_stillGeneratesRandomSevenCharacterCode() throws Exception {
        String body = mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/no-alias\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String shortCode = com.jayway.jsonpath.JsonPath.read(body, "$.shortCode");
        assertThat(shortCode).matches("[0-9a-zA-Z]{7}");
    }

    @Test
    void createWithValidCustomAlias_returns201WithAliasAsShortCode_andWorksAcrossAllThreeGetEndpoints() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/products\",\"customAlias\":\"products\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/urls/products")))
                .andExpect(jsonPath("$.shortCode").value("products"))
                .andExpect(jsonPath("$.shortUrl", org.hamcrest.Matchers.endsWith("/products")));

        // Redirect works for the alias.
        mockMvc.perform(get("/products"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/products"));

        // Details works for the alias.
        mockMvc.perform(get("/api/urls/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/products"))
                .andExpect(jsonPath("$.shortCode").value("products"));

        // Analytics works for the alias, and reflects the one redirect above.
        mockMvc.perform(get("/api/urls/products/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("products"))
                .andExpect(jsonPath("$.clickCount").value(1));

        assertThat(repository.existsByShortCode("products")).isTrue();
    }

    @Test
    void duplicateCustomAlias_returns409WithCentralizedErrorBody() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/first\",\"customAlias\":\"taken-alias\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/second\",\"customAlias\":\"taken-alias\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));

        // Only the first request's URL is stored — the conflicting one must not overwrite it.
        assertThat(repository.findByShortCode("taken-alias").orElseThrow().getOriginalUrl())
                .isEqualTo("https://example.com/first");
    }

    @Test
    void customAliasTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void customAliasTooLong_returns400() throws Exception {
        String tooLong = "a".repeat(31);
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void customAliasWithSpaces_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"has space\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void customAliasWithSlash_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"has/slash\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }

    @Test
    void customAliasWithUnsafeCharacters_returns400() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com\",\"customAlias\":\"abc?d=1&e=2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customAlias"));
    }
}
