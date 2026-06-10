package org.fpt.studydeck.controller.deck;

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
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsDeck() throws Exception {
        mockMvc.perform(post("/api/v1/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Korean Basics\",\"description\":\"Starter words\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Korean Basics"));
    }
}
