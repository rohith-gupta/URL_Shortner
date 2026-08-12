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

    @Test
    void redirect_shortCodeTooShort_doesNotMatchRoute_returns404() throws Exception {
        mockMvc.perform(get("/short"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_shortCodeTooLong_doesNotMatchRoute_returns404() throws Exception {
        mockMvc.perform(get("/toolongcode123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_shortCodeWithInvalidCharacter_doesNotMatchRoute_returns404() throws Exception {
        mockMvc.perform(get("/abc-123"))
                .andExpect(status().isNotFound());
    }
}
