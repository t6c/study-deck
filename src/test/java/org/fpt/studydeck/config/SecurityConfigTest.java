package org.fpt.studydeck.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest
@Import({SecurityConfig.class, SecurityConfigTest.PingController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsUnauthenticatedApiRequests() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk())
            .andExpect(content().string("pong"));
    }

    @RestController
    public static class PingController {

        @GetMapping("/api/v1/ping")
        String ping() {
            return "pong";
        }
    }
}
