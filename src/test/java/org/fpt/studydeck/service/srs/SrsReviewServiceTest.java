package org.fpt.studydeck.service.srs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsCardState;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.domain.srs.SrsState;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.repository.srs.SrsReviewLogRepository;
import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, FsrsScheduler.class, SrsReviewService.class})
class SrsReviewServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private SrsReviewService srsReviewService;

    @Autowired
    private SrsCardStateRepository srsCardStateRepository;

    @Autowired
    private SrsReviewLogRepository srsReviewLogRepository;

    @Test
    void reviewExistingFlashcardWithGoodSchedulesStateAndCreatesReviewLog() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        Instant beforeReview = Instant.now().minusSeconds(1);

        var response = srsReviewService.review(flashcard.getId(), new SrsReviewRequest(SrsRating.GOOD, 1200));

        assertThat(response.flashcardId()).isEqualTo(flashcard.getId());
        assertThat(response.rating()).isEqualTo(SrsRating.GOOD);
        assertThat(response.dueAt()).isAfter(beforeReview);
        assertThat(response.reps()).isEqualTo(1);
        assertThat(srsReviewLogRepository.count()).isEqualTo(1);
    }

    @Test
    void dueCardsIncludesNewFlashcardsWithNoSrsCardState() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        var dueCards = srsReviewService.dueCards(deck.getId(), Instant.parse("2026-01-01T00:00:00Z"));

        assertThat(dueCards)
            .extracting(card -> card.flashcardId())
            .containsExactly(flashcard.getId());
        assertThat(dueCards.get(0).state()).isEqualTo(SrsState.NEW);
    }

    @Test
    void dueCardsIncludesExistingDueStatesAndExcludesFutureStates() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var dueFlashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        var futureFlashcard = flashcardService.createFlashcard(deck.getId(), "가게", "store", null, null);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        srsCardStateRepository.save(reviewedState(dueFlashcard, now.minusSeconds(1)));
        srsCardStateRepository.save(reviewedState(futureFlashcard, now.plusSeconds(60)));

        var dueCards = srsReviewService.dueCards(deck.getId(), now);

        assertThat(dueCards)
            .extracting(card -> card.flashcardId())
            .containsExactly(dueFlashcard.getId());
    }

    @Test
    void getStateForNeverReviewedCardReturnsNew() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        var state = srsReviewService.getState(flashcard.getId());

        assertThat(state.flashcardId()).isEqualTo(flashcard.getId());
        assertThat(state.state()).isEqualTo(SrsState.NEW);
        assertThat(state.reps()).isZero();
        assertThat(state.lapses()).isZero();
    }

    @Test
    void rejectsNegativeReviewDuration() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        assertThatThrownBy(() -> srsReviewService.review(
                flashcard.getId(),
                new SrsReviewRequest(SrsRating.GOOD, -1)
            ))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("Duration must be zero or positive.");
    }

    private static SrsCardState reviewedState(org.fpt.studydeck.domain.deck.Flashcard flashcard, Instant dueAt) {
        SrsCardState state = SrsCardState.createNew(flashcard);
        state.applyReview(
            SrsRating.GOOD,
            SrsState.REVIEW,
            dueAt,
            2.5,
            4.0,
            1,
            0,
            1,
            0,
            "{\"due\":\"" + dueAt + "\"}",
            dueAt.minusSeconds(3600)
        );
        return state;
    }
}
