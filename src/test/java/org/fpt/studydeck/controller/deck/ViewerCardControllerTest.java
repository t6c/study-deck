package org.fpt.studydeck.controller.deck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser
class ViewerCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void getsViewerCards() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);

        mockMvc.perform(get("/api/v1/decks/{deckId}/viewer-cards", deck.getId())
                .param("sort", "original")
                .param("mode", "view"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].term").value("first"));
    }
}
