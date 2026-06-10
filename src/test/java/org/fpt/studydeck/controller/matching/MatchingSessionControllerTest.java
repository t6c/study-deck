package org.fpt.studydeck.controller.matching;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class MatchingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void createReturnsCreatedAndExpectedItemCount() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 3);

        mockMvc.perform(post("/api/v1/decks/{deckId}/matching-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardCount\":3,\"starredOnly\":false}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.cardCount").value(3))
            .andExpect(jsonPath("$.items.length()").value(3));
    }

    @Test
    void matchReturnsMatchedCountOne() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var created = createSession(deck.getId(), 2);
        var sessionId = com.jayway.jsonpath.JsonPath.read(created, "$.id");
        var itemId = com.jayway.jsonpath.JsonPath.read(created, "$.items[0].id");

        mockMvc.perform(post("/api/v1/matching-sessions/{sessionId}/matches", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemId\":" + itemId + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.matchedCount").value(1))
            .andExpect(jsonPath("$.items[0].matched").value(true));
    }

    @Test
    void getReturnsSession() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var created = createSession(deck.getId(), 2);
        var sessionId = com.jayway.jsonpath.JsonPath.read(created, "$.id");

        mockMvc.perform(get("/api/v1/matching-sessions/{sessionId}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(sessionId))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void completeReturnsCompleted() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var created = createSession(deck.getId(), 2);
        var sessionId = com.jayway.jsonpath.JsonPath.read(created, "$.id");

        mockMvc.perform(post("/api/v1/matching-sessions/{sessionId}/complete", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.durationMs").isNumber());
    }

    @Test
    void createRejectsNonPositiveCardCount() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        mockMvc.perform(post("/api/v1/decks/{deckId}/matching-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardCount\":0,\"starredOnly\":false}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void matchRejectsMissingItemId() throws Exception {
        mockMvc.perform(post("/api/v1/matching-sessions/{sessionId}/matches", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    private String createSession(Long deckId, int cardCount) throws Exception {
        return mockMvc.perform(post("/api/v1/decks/{deckId}/matching-sessions", deckId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardCount\":" + cardCount + ",\"starredOnly\":false}"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    private void createCards(Long deckId, int count) {
        for (int index = 1; index <= count; index++) {
            flashcardService.createFlashcard(deckId, "term " + index, "definition " + index, null, null);
        }
    }
}
