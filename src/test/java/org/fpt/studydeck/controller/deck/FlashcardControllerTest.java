package org.fpt.studydeck.controller.deck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void starsFlashcard() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        mockMvc.perform(patch("/api/v1/flashcards/{flashcardId}/star", flashcard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"starred\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.starred").value(true));
    }

    @Test
    void rejectsMissingStarredFlag() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        mockMvc.perform(patch("/api/v1/flashcards/{flashcardId}/star", flashcard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("starred"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Starred flag is required."));
    }
}
