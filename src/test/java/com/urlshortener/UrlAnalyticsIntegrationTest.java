package com.urlshortener;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test for GET /api/urls/{shortCode}/analytics: real controller, real service, real
 * repository, real (H2, test-scope) database. This is what actually proves clickCount tracks
 * real redirects and nothing else does — the service/controller unit tests can't, since
 * {@link UrlMapping}'s clickCount is only ever mutated by the real atomic UPDATE.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlAnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void analytics_tracksRealRedirects_andNothingElseMutatesTheCount() throws Exception {
        UrlMapping saved = repository.saveAndFlush(new UrlMapping("https://example.com/analytics-target", "analyt1"));
        String shortCode = saved.getShortCode();

        // New URL: clickCount starts at 0.
        mockMvc.perform(get("/api/urls/" + shortCode + "/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/analytics-target"))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.createdAt").exists());

        // One redirect -> 1.
        mockMvc.perform(get("/" + shortCode)).andExpect(status().isFound());
        mockMvc.perform(get("/api/urls/" + shortCode + "/analytics"))
                .andExpect(jsonPath("$.clickCount").value(1));

        // A second redirect -> 2.
        mockMvc.perform(get("/" + shortCode)).andExpect(status().isFound());
        mockMvc.perform(get("/api/urls/" + shortCode + "/analytics"))
                .andExpect(jsonPath("$.clickCount").value(2));

        // Calling analytics again, with no redirect in between, must not itself bump the count.
        mockMvc.perform(get("/api/urls/" + shortCode + "/analytics"))
                .andExpect(jsonPath("$.clickCount").value(2));

        // Nor must a details call.
        mockMvc.perform(get("/api/urls/" + shortCode)).andExpect(status().isOk());
        mockMvc.perform(get("/api/urls/" + shortCode + "/analytics"))
                .andExpect(jsonPath("$.clickCount").value(2));
    }

    @Test
    void analytics_unknownShortCode_returns404WithCentralizedErrorBody() throws Exception {
        mockMvc.perform(get("/api/urls/zzzzzzz/analytics"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void analytics_malformedShortCode_returns404() throws Exception {
        // 3 chars — below the 4-char alias minimum (route widened for custom aliases; see
        // docs/AI_WORKLOG.md, "Brownfield: add optional custom aliases").
        mockMvc.perform(get("/api/urls/abc/analytics")).andExpect(status().isNotFound());
    }

    @Test
    void existingCreateEndpoint_stillWorksAlongsideAnalytics() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/still-works\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists());
    }
}
