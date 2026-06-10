package org.fpt.studydeck.controller.learn;

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
class LearnSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void postCreateReturnsCreatedSession() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);

        mockMvc.perform(post("/api/v1/decks/{deckId}/learn-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lengthOfRounds":7,"flashcards":false,"multipleChoice":true,"written":false,"trueFalse":false,"starredOnly":false,"shuffleTerms":false}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.totalItems").value(1))
            .andExpect(jsonPath("$.items[0].questionType").value("MULTIPLE_CHOICE"));
    }

    @Test
    void postAnswerReturnsCorrectCount() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var created = mockMvc.perform(post("/api/v1/decks/{deckId}/learn-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lengthOfRounds":0,"flashcards":true,"multipleChoice":false,"written":false,"trueFalse":false,"starredOnly":false,"shuffleTerms":false}"""))
            .andReturn();
        var sessionId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var itemId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.items[0].id");

        mockMvc.perform(post("/api/v1/learn-sessions/{sessionId}/answers", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemId\":" + itemId + ",\"answer\":\" hello \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correctCount").value(1))
            .andExpect(jsonPath("$.wrongCount").value(0));
    }

    @Test
    void postCompleteReturnsCompletedSession() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var created = mockMvc.perform(post("/api/v1/decks/{deckId}/learn-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lengthOfRounds":0,"flashcards":true,"multipleChoice":false,"written":false,"trueFalse":false,"starredOnly":false,"shuffleTerms":false}"""))
            .andReturn();
        var sessionId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/learn-sessions/{sessionId}/complete", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void postAnswerRejectsMissingItemIdAndBlankAnswer() throws Exception {
        mockMvc.perform(post("/api/v1/learn-sessions/{sessionId}/answers", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answer\":\"hello\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("itemId"));

        mockMvc.perform(post("/api/v1/learn-sessions/{sessionId}/answers", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"itemId\":1,\"answer\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("answer"));
    }

    @Test
    void postCreateRejectsNegativeLengthOfRounds() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);

        mockMvc.perform(post("/api/v1/decks/{deckId}/learn-sessions", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lengthOfRounds":-1,"flashcards":true,"multipleChoice":false,"written":false,"trueFalse":false,"starredOnly":false,"shuffleTerms":false}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("lengthOfRounds"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Length of rounds must be zero or positive."));
    }
}
