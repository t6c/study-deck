package org.fpt.studydeck.controller.auth;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registersUserAndReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "student@example.com",
                      "password": "anything",
                      "displayName": "Vocabulary Student"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
            .andExpect(jsonPath("$.expiresInSeconds").value(86400))
            .andExpect(jsonPath("$.user.email").value("student@example.com"))
            .andExpect(jsonPath("$.user.displayName").value("Vocabulary Student"));
    }

    @Test
    void logsInRegisteredUserAndReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login@example.com",
                      "password": "plain",
                      "displayName": "Login Student"
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "login@example.com",
                      "password": "plain"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
            .andExpect(jsonPath("$.user.email").value("login@example.com"));
    }

    @Test
    void rejectsDuplicateEmailRegistration() throws Exception {
        String request = """
            {
              "email": "duplicate@example.com",
              "password": "anything",
              "displayName": "First Student"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email is already registered."));
    }

    @Test
    void rejectsInvalidLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "missing@example.com",
                      "password": "wrong"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }
}
