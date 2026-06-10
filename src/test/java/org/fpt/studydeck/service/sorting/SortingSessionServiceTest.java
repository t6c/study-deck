package org.fpt.studydeck.service.sorting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.sorting.SortingAnswer;
import org.fpt.studydeck.domain.sorting.SortingSessionStatus;
import org.fpt.studydeck.dto.sorting.CreateSortingSessionRequest;
import org.fpt.studydeck.dto.sorting.SortingAnswerRequest;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.repository.sorting.SortingSessionRepository;
import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.fpt.studydeck.service.deck.ViewerCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, ViewerCardService.class, SortingSessionService.class})
class SortingSessionServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private SortingSessionService sortingSessionService;

    @Autowired
    private SortingSessionRepository sortingSessionRepository;

    @Test
    void createSessionCreatesOneItemPerSelectedCardAndActiveStatus() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);

        var session = sortingSessionService.createSession(
            deck.getId(),
            new CreateSortingSessionRequest(false, false)
        );

        assertThat(session.status()).isEqualTo(SortingSessionStatus.ACTIVE.name());
        assertThat(session.items()).hasSize(2);
        assertThat(session.knownCount()).isZero();
        assertThat(session.doNotKnowCount()).isZero();
    }

    @Test
    void answerKnowIncrementsKnownCount() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        var session = sortingSessionService.createSession(deck.getId(), new CreateSortingSessionRequest(false, false));

        var answered = sortingSessionService.answer(
            session.id(),
            new SortingAnswerRequest(session.items().get(0).id(), SortingAnswer.KNOW)
        );

        assertThat(answered.knownCount()).isEqualTo(1);
        assertThat(answered.doNotKnowCount()).isZero();
    }

    @Test
    void answerDoNotKnowIncrementsDoNotKnowCount() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        var session = sortingSessionService.createSession(deck.getId(), new CreateSortingSessionRequest(false, false));

        var answered = sortingSessionService.answer(
            session.id(),
            new SortingAnswerRequest(session.items().get(0).id(), SortingAnswer.DO_NOT_KNOW)
        );

        assertThat(answered.knownCount()).isZero();
        assertThat(answered.doNotKnowCount()).isEqualTo(1);
    }

    @Test
    void answeringAllItemsCompletesSession() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        var session = sortingSessionService.createSession(deck.getId(), new CreateSortingSessionRequest(false, false));

        var firstAnswer = sortingSessionService.answer(
            session.id(),
            new SortingAnswerRequest(session.items().get(0).id(), SortingAnswer.KNOW)
        );
        var completed = sortingSessionService.answer(
            session.id(),
            new SortingAnswerRequest(firstAnswer.items().get(1).id(), SortingAnswer.DO_NOT_KNOW)
        );

        assertThat(completed.status()).isEqualTo(SortingSessionStatus.COMPLETED.name());
        assertThat(sortingSessionRepository.findById(session.id()))
            .get()
            .extracting(stored -> stored.getCompletedAt())
            .isNotNull();
    }

    @Test
    void starredOnlyWithNoStarredCardsThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);

        assertThatThrownBy(() -> sortingSessionService.createSession(
                deck.getId(),
                new CreateSortingSessionRequest(true, false)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("No cards are available for sorting.");
    }

    @Test
    void deckWithNoCardsThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        assertThatThrownBy(() -> sortingSessionService.createSession(
                deck.getId(),
                new CreateSortingSessionRequest(false, false)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("No cards are available for sorting.");
    }
}
