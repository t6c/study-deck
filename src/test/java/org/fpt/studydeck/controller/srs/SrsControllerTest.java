package org.fpt.studydeck.controller.srs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class SrsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void getDueReturnsNewCards() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        mockMvc.perform(get("/api/v1/decks/{deckId}/srs/due", deck.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].flashcardId").value(flashcard.getId()))
            .andExpect(jsonPath("$[0].state").value("NEW"));
    }

    @Test
    void postReviewEndpointUpdatesStateAndReturnsRatingAndState() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        mockMvc.perform(post("/api/v1/flashcards/{flashcardId}/srs/reviews", flashcard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":\"GOOD\",\"durationMs\":1200}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flashcardId").value(flashcard.getId()))
            .andExpect(jsonPath("$.rating").value("GOOD"))
            .andExpect(jsonPath("$.state").value("LEARNING"));

        mockMvc.perform(get("/api/v1/flashcards/{flashcardId}/srs-state", flashcard.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("LEARNING"))
            .andExpect(jsonPath("$.reps").value(1));
    }

    @Test
    void rejectsNegativeReviewDuration() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        mockMvc.perform(post("/api/v1/flashcards/{flashcardId}/srs/reviews", flashcard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":\"GOOD\",\"durationMs\":-1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("durationMs"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Duration must be zero or positive."));
    }

    @Test
    void getStatsReturnsSrsCounts() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var reviewed = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        flashcardService.createFlashcard(deck.getId(), "가게", "store", null, null);

        mockMvc.perform(post("/api/v1/flashcards/{flashcardId}/srs/reviews", reviewed.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":\"GOOD\",\"durationMs\":1200}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/decks/{deckId}/srs/stats", deck.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.newCards").value(1))
            .andExpect(jsonPath("$.learningCards").value(1))
            .andExpect(jsonPath("$.reviewCards").value(0))
            .andExpect(jsonPath("$.dueCards").value(1));
    }
}
