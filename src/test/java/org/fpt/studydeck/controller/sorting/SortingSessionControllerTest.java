package org.fpt.studydeck.controller.sorting;

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
class SortingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void createsGetsAndAnswersSortingSession() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);

        var created = mockMvc.perform(post("/api/v1/decks/{deckId}/sorting-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"starredOnly\":false,\"shuffle\":false}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn();

        var sessionId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var itemId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.items[0].id");

        mockMvc.perform(get("/api/v1/sorting-sessions/{sessionId}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].term").value("first"));

        mockMvc.perform(post("/api/v1/sorting-sessions/{sessionId}/answers", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemId\":" + itemId + ",\"answer\":\"KNOW\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.knownCount").value(1))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
