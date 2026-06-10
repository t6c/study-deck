package org.fpt.studydeck.service.practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.practice.PracticeTestStatus;
import org.fpt.studydeck.dto.practice.CreatePracticeTestRequest;
import org.fpt.studydeck.dto.practice.PracticeAnswerRequest;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.fpt.studydeck.service.deck.ViewerCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, ViewerCardService.class, PracticeTestService.class})
class PracticeTestServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private PracticeTestService practiceTestService;

    @Test
    void createPracticeTestStartsActiveWithRequestedQuestions() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 6);

        var test = practiceTestService.createPracticeTest(
            deck.getId(),
            new CreatePracticeTestRequest(5, true, true, true, false, false, true)
        );

        assertThat(test.status()).isEqualTo(PracticeTestStatus.ACTIVE.name());
        assertThat(test.questionCount()).isEqualTo(5);
        assertThat(test.answeredCount()).isZero();
        assertThat(test.questions()).hasSize(5);
        assertThat(test.questions())
            .extracting("questionType")
            .containsExactly(
                LearnQuestionType.MULTIPLE_CHOICE,
                LearnQuestionType.WRITTEN,
                LearnQuestionType.TRUE_FALSE,
                LearnQuestionType.MULTIPLE_CHOICE,
                LearnQuestionType.WRITTEN
            );
    }

    @Test
    void answerCorrectIncrementsAnsweredCountAndMarksQuestionCorrect() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var test = practiceTestService.createPracticeTest(
            deck.getId(),
            new CreatePracticeTestRequest(1, false, true, false, false, false, true)
        );

        var answered = practiceTestService.answer(
            test.id(),
            new PracticeAnswerRequest(test.questions().get(0).id(), "  HELLO  ")
        );

        assertThat(answered.answeredCount()).isEqualTo(1);
        assertThat(answered.questions().get(0).submittedAnswer()).isEqualTo("  HELLO  ");
        assertThat(answered.questions().get(0).correct()).isTrue();
    }

    @Test
    void answerWrongMarksQuestionIncorrect() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var test = practiceTestService.createPracticeTest(
            deck.getId(),
            new CreatePracticeTestRequest(1, false, true, false, false, false, true)
        );

        var answered = practiceTestService.answer(
            test.id(),
            new PracticeAnswerRequest(test.questions().get(0).id(), "goodbye")
        );

        assertThat(answered.answeredCount()).isEqualTo(1);
        assertThat(answered.questions().get(0).correct()).isFalse();
    }

    @Test
    void submitMarksSubmittedAndScoresUnansweredAsIncorrect() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 4);
        var test = practiceTestService.createPracticeTest(
            deck.getId(),
            new CreatePracticeTestRequest(4, false, true, false, false, false, true)
        );
        practiceTestService.answer(test.id(), new PracticeAnswerRequest(test.questions().get(0).id(), "definition 1"));
        practiceTestService.answer(test.id(), new PracticeAnswerRequest(test.questions().get(1).id(), "wrong"));

        var submitted = practiceTestService.submit(test.id());

        assertThat(submitted.status()).isEqualTo(PracticeTestStatus.SUBMITTED.name());
        assertThat(submitted.scorePercent()).isEqualTo(25.0);
    }

    @Test
    void noQuestionTypeSelectedThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);

        assertThatThrownBy(() -> practiceTestService.createPracticeTest(
                deck.getId(),
                new CreatePracticeTestRequest(1, false, false, false, false, false, true)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("At least one question type is required.");
    }

    @Test
    void noAnswerFormatSelectedThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);

        assertThatThrownBy(() -> practiceTestService.createPracticeTest(
                deck.getId(),
                new CreatePracticeTestRequest(1, true, false, false, false, false, false)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("At least one answer format is required.");
    }

    @Test
    void starredOnlyInsufficientCardsThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var starred = flashcardService.createFlashcard(deck.getId(), "term 1", "definition 1", null, null);
        flashcardService.createFlashcard(deck.getId(), "term 2", "definition 2", null, null);
        flashcardService.setStarred(starred.getId(), true);

        assertThatThrownBy(() -> practiceTestService.createPracticeTest(
                deck.getId(),
                new CreatePracticeTestRequest(2, true, false, false, true, false, true)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("Not enough cards are available for practice test.");
    }

    @Test
    void submittedTestRejectsFutureAnswers() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var test = practiceTestService.createPracticeTest(
            deck.getId(),
            new CreatePracticeTestRequest(1, false, true, false, false, false, true)
        );
        practiceTestService.submit(test.id());

        assertThatThrownBy(() -> practiceTestService.answer(
                test.id(),
                new PracticeAnswerRequest(test.questions().get(0).id(), "hello")
            ))
            .isInstanceOf(ResourceConflictException.class)
            .hasMessage("Practice test is already submitted.");
    }

    private void createCards(Long deckId, int count) {
        for (int index = 1; index <= count; index++) {
            flashcardService.createFlashcard(deckId, "term " + index, "definition " + index, null, null);
        }
    }
}
