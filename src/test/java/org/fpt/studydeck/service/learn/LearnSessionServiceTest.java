package org.fpt.studydeck.service.learn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.LearnSessionStatus;
import org.fpt.studydeck.dto.learn.CreateLearnSessionRequest;
import org.fpt.studydeck.dto.learn.LearnAnswerRequest;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.repository.learn.LearnSessionRepository;
import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.fpt.studydeck.service.deck.ViewerCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, ViewerCardService.class, LearnSessionService.class})
class LearnSessionServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private LearnSessionService learnSessionService;

    @Autowired
    private LearnSessionRepository learnSessionRepository;

    @Test
    void createSessionCapsItemsAtRequestedRoundLengthAndStartsActive() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        for (int index = 1; index <= 9; index++) {
            flashcardService.createFlashcard(deck.getId(), "term " + index, "definition " + index, null, null);
        }

        var session = learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(7, false, true, false, false, false, false)
        );

        assertThat(session.status()).isEqualTo(LearnSessionStatus.ACTIVE.name());
        assertThat(session.totalItems()).isEqualTo(7);
        assertThat(session.items()).hasSize(7);
        assertThat(session.items()).extracting("questionType").containsOnly(LearnQuestionType.MULTIPLE_CHOICE);
    }

    @Test
    void answerCorrectNormalizedExactIncrementsCorrectCount() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var session = learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(0, true, false, false, false, false, false)
        );

        var answered = learnSessionService.answer(
            session.id(),
            new LearnAnswerRequest(session.items().get(0).id(), "  HELLO  ")
        );

        assertThat(answered.correctCount()).isEqualTo(1);
        assertThat(answered.wrongCount()).isZero();
        assertThat(answered.items().get(0).attempts()).isEqualTo(1);
    }

    @Test
    void answerWrongIncrementsWrongCount() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var session = learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(0, true, false, false, false, false, false)
        );

        var answered = learnSessionService.answer(
            session.id(),
            new LearnAnswerRequest(session.items().get(0).id(), "goodbye")
        );

        assertThat(answered.correctCount()).isZero();
        assertThat(answered.wrongCount()).isEqualTo(1);
        assertThat(answered.items().get(0).attempts()).isEqualTo(1);
    }

    @Test
    void trueFalseQuestionUsesBooleanAnswerContract() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var session = learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(0, false, false, false, true, false, false)
        );

        assertThat(session.items().get(0).questionType()).isEqualTo(LearnQuestionType.TRUE_FALSE);
        assertThat(session.items().get(0).prompt()).isEqualTo("annyeong = hello");
        assertThat(session.items().get(0).answer()).isEqualTo("true");

        var correct = learnSessionService.answer(
            session.id(),
            new LearnAnswerRequest(session.items().get(0).id(), " TRUE ")
        );
        var wrong = learnSessionService.answer(
            session.id(),
            new LearnAnswerRequest(session.items().get(0).id(), "false")
        );

        assertThat(correct.correctCount()).isEqualTo(1);
        assertThat(correct.wrongCount()).isZero();
        assertThat(wrong.correctCount()).isEqualTo(1);
        assertThat(wrong.wrongCount()).isEqualTo(1);
    }

    @Test
    void negativeLengthOfRoundsThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);

        assertThatThrownBy(() -> learnSessionService.createSession(
                deck.getId(),
                new CreateLearnSessionRequest(-1, true, false, false, false, false, false)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("Length of rounds must be zero or positive.");
    }

    @Test
    void completedSessionRejectsAnswer() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var session = learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(0, true, false, false, false, false, false)
        );
        learnSessionService.complete(session.id());

        assertThatThrownBy(() -> learnSessionService.answer(
                session.id(),
                new LearnAnswerRequest(session.items().get(0).id(), "hello")
            ))
            .isInstanceOf(ResourceConflictException.class)
            .hasMessage("Learn session is already completed.");
    }

    @Test
    void starredOnlyWithNoStarredCardsThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);

        assertThatThrownBy(() -> learnSessionService.createSession(
                deck.getId(),
                new CreateLearnSessionRequest(0, true, false, false, false, true, false)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("No cards are available for learn mode.");
    }

    @Test
    void noQuestionTypeSelectedThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);

        assertThatThrownBy(() -> learnSessionService.createSession(
                deck.getId(),
                new CreateLearnSessionRequest(0, false, false, false, false, false, false)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("At least one question type is required.");
    }

    @Test
    void completeMarksSessionCompleted() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "annyeong", "hello", null, null);
        var session = learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(0, true, false, false, false, false, false)
        );

        var completed = learnSessionService.complete(session.id());

        assertThat(completed.status()).isEqualTo(LearnSessionStatus.COMPLETED.name());
        assertThat(learnSessionRepository.findById(session.id()))
            .get()
            .extracting(stored -> stored.getCompletedAt())
            .isNotNull();
    }
}
