package com.urlshortener;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test for GET /api/urls/{shortCode}: real controller, real service, real
 * repository, real (H2, test-scope) database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlDetailsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void getUrlDetails_existingShortCode_returns200WithCorrectFields_andNeverMutatesClickCount() throws Exception {
        UrlMapping saved = repository.saveAndFlush(new UrlMapping("https://example.com/details-target", "detail1"));

        mockMvc.perform(get("/api/urls/" + saved.getShortCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/details-target"))
                .andExpect(jsonPath("$.shortCode").value("detail1"))
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.clickCount").doesNotExist());
        assertThat(repository.findByShortCode("detail1").orElseThrow().getClickCount()).isZero();

        // The redirect endpoint (still working, unmodified by this slice) is what actually
        // increments the count...
        mockMvc.perform(get("/" + saved.getShortCode())).andExpect(status().isFound());
        assertThat(repository.findByShortCode("detail1").orElseThrow().getClickCount()).isEqualTo(1);

        // ...and a second details call afterward must leave that count exactly where it was.
        mockMvc.perform(get("/api/urls/" + saved.getShortCode())).andExpect(status().isOk());
        assertThat(repository.findByShortCode("detail1").orElseThrow().getClickCount()).isEqualTo(1);
    }

    @Test
    void getUrlDetails_unknownShortCode_returns404WithCentralizedErrorBody() throws Exception {
        mockMvc.perform(get("/api/urls/zzzzzzz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getUrlDetails_malformedShortCode_returns404() throws Exception {
        // 3 chars — below the 4-char alias minimum (route widened for custom aliases; see
        // docs/AI_WORKLOG.md, "Brownfield: add optional custom aliases").
        mockMvc.perform(get("/api/urls/abc")).andExpect(status().isNotFound());
    }

    @Test
    void existingCreateEndpoint_stillWorksAlongsideDetails() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/still-works\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists());
    }
}
