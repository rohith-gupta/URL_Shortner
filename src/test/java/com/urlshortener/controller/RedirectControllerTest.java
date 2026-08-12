package com.urlshortener.controller;

import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-slice tests: HTTP contract only. {@link UrlService} is mocked — the resolve/
 * increment logic itself is covered by {@code UrlServiceTest} and the full-stack integration
 * test.
 */
@WebMvcTest(RedirectController.class)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void redirect_existingShortCode_returns302WithLocation() throws Exception {
        when(urlService.resolveAndRecordRedirect("abc1234")).thenReturn("https://example.com/target");

        mockMvc.perform(get("/abc1234"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/target"));
    }

    @Test
    void redirect_unknownShortCode_returns404() throws Exception {
        when(urlService.resolveAndRecordRedirect("zzzzzzz"))
                .thenThrow(new ShortCodeNotFoundException("zzzzzzz"));

        mockMvc.perform(get("/zzzzzzz"))
                .andExpect(status().isNotFound());
    }

    // Route accepts 4-30 chars of [0-9a-zA-Z_-] (widened for custom aliases — see
    // docs/AI_WORKLOG.md, "Brownfield: add optional custom aliases"). These examples are
    // chosen to still be genuinely outside that range/charset, not just outside the old
    // fixed-7 rule — "short" (5 chars) and "abc-123" (contains a now-valid hyphen) used to be
    // malformed examples here but no longer are.

    @Test
    void redirect_shortCodeTooShort_doesNotMatchRoute_returns404() throws Exception {
        mockMvc.perform(get("/abc")) // 3 chars, below the 4-character minimum
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_shortCodeTooLong_doesNotMatchRoute_returns404() throws Exception {
        mockMvc.perform(get("/" + "a".repeat(31))) // above the 30-character maximum
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_shortCodeWithInvalidCharacter_doesNotMatchRoute_returns404() throws Exception {
        mockMvc.perform(get("/abc.de")) // '.' is not in [0-9a-zA-Z_-]
                .andExpect(status().isNotFound());
    }
}
