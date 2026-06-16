package org.fpt.studydeck.controller.deck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.fpt.studydeck.service.deck.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Test
    void createsDeck() throws Exception {
        mockMvc.perform(post("/api/v1/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Korean Basics\",\"description\":\"Starter words\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Korean Basics"));
    }

    @Test
    void returnsNotFoundWhenRemovingDeckFromMissingFolder() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        mockMvc.perform(delete("/api/v1/folders/{folderId}/decks/{deckId}", 999L, deck.getId()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Folder was not found."));
    }
}
