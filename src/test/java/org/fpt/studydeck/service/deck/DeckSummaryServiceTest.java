package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.srs.SrsCardState;
import org.junit.jupiter.api.Test;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.domain.srs.SrsState;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.service.srs.FsrsScheduler;
import org.fpt.studydeck.service.srs.SrsReviewService;
import org.fpt.studydeck.service.srs.SrsStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
    DeckService.class,
    FlashcardService.class,
    FsrsScheduler.class,
    SrsReviewService.class,
    SrsStatsService.class,
    DeckSummaryService.class
})
class DeckSummaryServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private DeckSummaryService deckSummaryService;

    @Autowired
    private SrsReviewService srsReviewService;

    @Autowired
    private SrsCardStateRepository srsCardStateRepository;

    @Test
    void returnsDeckSummaryWithCardCountsAndAvailableModes() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        var starred = flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        flashcardService.setStarred(starred.getId(), true);
        var reviewed = flashcardService.createFlashcard(deck.getId(), "third", "three", null, null);
        srsReviewService.review(reviewed.getId(), new SrsReviewRequest(SrsRating.GOOD, 1200));
        var relearning = flashcardService.createFlashcard(deck.getId(), "fourth", "four", null, null);
        srsCardStateRepository.save(reviewedState(relearning, SrsState.RELEARNING));

        var summary = deckSummaryService.getSummary(deck.getId());

        assertThat(summary.deckId()).isEqualTo(deck.getId());
        assertThat(summary.totalCards()).isEqualTo(4);
        assertThat(summary.starredCards()).isEqualTo(1);
        assertThat(summary.dueSrsCards()).isEqualTo(2);
        assertThat(summary.newCards()).isEqualTo(2);
        assertThat(summary.learningCards()).isEqualTo(2);
        assertThat(summary.reviewCards()).isZero();
        assertThat(summary.availableModes())
            .contains("FLASHCARDS", "LEARN", "MATCH", "PRACTICE_TEST", "SPACED_REPETITION");
    }

    private static SrsCardState reviewedState(Flashcard flashcard, SrsState state) {
        Instant dueAt = Instant.now().plusSeconds(60);
        SrsCardState cardState = SrsCardState.createNew(flashcard);
        cardState.applyReview(
            SrsRating.AGAIN,
            state,
            dueAt,
            2.5,
            4.0,
            1,
            0,
            2,
            1,
            "{\"due\":\"" + dueAt + "\"}",
            dueAt.minusSeconds(3600)
        );
        return cardState;
    }
}
