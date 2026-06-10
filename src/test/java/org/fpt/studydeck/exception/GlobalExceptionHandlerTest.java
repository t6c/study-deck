package org.fpt.studydeck.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.fpt.studydeck.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest
@Import({GlobalExceptionHandler.class, SecurityConfig.class, GlobalExceptionHandlerTest.ThrowingController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsNotFoundApiErrorForResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/throw/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Deck was not found."))
            .andExpect(jsonPath("$.path").value("/throw/not-found"));
    }

    @Test
    void apiErrorResponseContainsFieldErrors() {
        FieldErrorResponse field = new FieldErrorResponse("title", "Title is required.");
        ApiErrorResponse response = new ApiErrorResponse(
            "2026-06-10T00:00:00Z",
            400,
            "Bad Request",
            "Validation failed.",
            "/api/v1/decks",
            List.of(field)
        );

        assertThat(response.fieldErrors()).containsExactly(field);
    }

    @RestController
    public static class ThrowingController {

        @GetMapping("/throw/not-found")
        String notFound() {
            throw new ResourceNotFoundException("Deck was not found.");
        }
    }
}
