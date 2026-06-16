package org.fpt.studydeck.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.ThrowingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.ThrowingController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private org.fpt.studydeck.security.JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

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
    void returnsBadRequestApiErrorForInvalidRequestException() throws Exception {
        mockMvc.perform(get("/throw/invalid-request"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Deck title is invalid."))
            .andExpect(jsonPath("$.path").value("/throw/invalid-request"));
    }

    @Test
    void returnsConflictApiErrorForResourceConflictException() throws Exception {
        mockMvc.perform(get("/throw/conflict"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Conflict"))
            .andExpect(jsonPath("$.message").value("Deck already exists."))
            .andExpect(jsonPath("$.path").value("/throw/conflict"));
    }

    @Test
    void returnsValidationApiErrorForMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/throw/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Validation failed."))
            .andExpect(jsonPath("$.path").value("/throw/validation"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("title"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Title is required."));
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

        @GetMapping("/throw/invalid-request")
        String invalidRequest() {
            throw new InvalidRequestException("Deck title is invalid.");
        }

        @GetMapping("/throw/conflict")
        String conflict() {
            throw new ResourceConflictException("Deck already exists.");
        }

        @PostMapping("/throw/validation")
        String validation(@Valid @RequestBody CreateDeckRequest request) {
            return request.title();
        }
    }

    record CreateDeckRequest(
        @NotBlank(message = "Title is required.")
        String title
    ) {
    }
}
