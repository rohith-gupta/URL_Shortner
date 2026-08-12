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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack test for GET /{shortCode}: real controller, real service, real repository, real
 * (H2, test-scope) database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RedirectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    void redirect_existingShortCode_returns302AndIncrementsClickCountOnEachCall() throws Exception {
        UrlMapping saved = repository.saveAndFlush(new UrlMapping("https://example.com/redirect-target", "redir01"));

        mockMvc.perform(get("/" + saved.getShortCode()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/redirect-target"));
        assertThat(repository.findByShortCode("redir01").orElseThrow().getClickCount()).isEqualTo(1);

        mockMvc.perform(get("/" + saved.getShortCode()))
                .andExpect(status().isFound());
        assertThat(repository.findByShortCode("redir01").orElseThrow().getClickCount()).isEqualTo(2);
    }

    @Test
    void redirect_unknownButWellFormedShortCode_returns404WithCentralizedErrorBody() throws Exception {
        mockMvc.perform(get("/zzzzzzz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void redirect_malformedShortCode_returns404() throws Exception {
        mockMvc.perform(get("/short")).andExpect(status().isNotFound());
        mockMvc.perform(get("/toolongcode123")).andExpect(status().isNotFound());
        mockMvc.perform(get("/abc-123")).andExpect(status().isNotFound());
    }

    @Test
    void existingCreateEndpoint_stillWorksAlongsideRedirect() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType("application/json")
                        .content("{\"originalUrl\":\"https://example.com/still-works\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").exists());
    }
}
