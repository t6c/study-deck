package org.fpt.studydeck.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fpt.studydeck.security.JwtAuthenticationFilter;
import org.fpt.studydeck.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigTest.PingController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    SecurityConfigTest.PingController.class,
    SecurityConfigTest.SecurityTestBeans.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void rejectsUnauthenticatedApiRequests() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedApiRequestsWithBearerToken() throws Exception {
        String token = jwtService.generateToken("student@example.com");

        mockMvc.perform(get("/api/v1/ping")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("pong"));
    }

    @Test
    void keepsSwaggerAndOpenApiPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().isNotFound());
    }

    @RestController
    public static class PingController {

        @GetMapping("/api/v1/ping")
        String ping() {
            return "pong";
        }
    }

    @TestConfiguration
    static class SecurityTestBeans {

        @Bean
        JwtService jwtService() {
            return new JwtService(
                "test-secret-that-is-long-enough-for-hmac-sha256-signatures",
                86400
            );
        }

        @Bean
        UserDetailsService userDetailsService() {
            return username -> User.withUsername(username)
                .password("{noop}ignored")
                .authorities("ROLE_USER")
                .build();
        }
    }
}
