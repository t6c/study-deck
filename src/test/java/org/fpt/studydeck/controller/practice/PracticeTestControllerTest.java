package org.fpt.studydeck.controller.practice;

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
class PracticeTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void postCreateReturnsCreatedPracticeTest() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 3);

        mockMvc.perform(post("/api/v1/decks/{deckId}/practice-tests", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"questionCount":2,"multipleChoice":true,"written":false,"trueFalse":false,"starredOnly":false,"answerWithTerm":false,"answerWithDefinition":true}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.questionCount").value(2))
            .andExpect(jsonPath("$.questions[0].questionType").value("MULTIPLE_CHOICE"));
    }

    @Test
    void getPracticeTestReturnsOk() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var created = createPracticeTest(deck.getId(), 2);
        var practiceTestId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/practice-tests/{practiceTestId}", practiceTestId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(practiceTestId))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void postAnswerReturnsAnsweredCount() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var created = createPracticeTest(deck.getId(), 1);
        var practiceTestId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        var questionId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.questions[0].id");

        mockMvc.perform(post("/api/v1/practice-tests/{practiceTestId}/answers", practiceTestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":" + questionId + ",\"answer\":\" hello \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.answeredCount").value(1))
            .andExpect(jsonPath("$.questions[0].correct").value(true));
    }

    @Test
    void postSubmitReturnsSubmittedPracticeTest() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var created = createPracticeTest(deck.getId(), 1);
        var practiceTestId = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/practice-tests/{practiceTestId}/submit", practiceTestId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void postAnswerRejectsMissingQuestionIdAndBlankAnswer() throws Exception {
        mockMvc.perform(post("/api/v1/practice-tests/{practiceTestId}/answers", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"answer\":\"hello\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("questionId"));

        mockMvc.perform(post("/api/v1/practice-tests/{practiceTestId}/answers", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"questionId\":1,\"answer\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("answer"));
    }

    @Test
    void postCreateRejectsNonPositiveQuestionCount() throws Exception {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);

        mockMvc.perform(post("/api/v1/decks/{deckId}/practice-tests", deck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"questionCount":0,"multipleChoice":true,"written":false,"trueFalse":false,"starredOnly":false,"answerWithTerm":false,"answerWithDefinition":true}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[0].field").value("questionCount"))
            .andExpect(jsonPath("$.fieldErrors[0].message").value("Question count must be positive."));
    }

    private org.springframework.test.web.servlet.MvcResult createPracticeTest(Long deckId, int questionCount)
        throws Exception {
        return mockMvc.perform(post("/api/v1/decks/{deckId}/practice-tests", deckId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"questionCount":%d,"multipleChoice":false,"written":true,"trueFalse":false,"starredOnly":false,"answerWithTerm":false,"answerWithDefinition":true}"""
                    .formatted(questionCount)))
            .andReturn();
    }

    private void createCards(Long deckId, int count) {
        for (int index = 1; index <= count; index++) {
            flashcardService.createFlashcard(deckId, "term " + index, "definition " + index, null, null);
        }
    }
}
