package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
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

    @Test
    void returnsDeckSummaryWithCardCountsAndAvailableModes() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        var starred = flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        flashcardService.setStarred(starred.getId(), true);
        var reviewed = flashcardService.createFlashcard(deck.getId(), "third", "three", null, null);
        srsReviewService.review(reviewed.getId(), new SrsReviewRequest(SrsRating.GOOD, 1200));

        var summary = deckSummaryService.getSummary(deck.getId());

        assertThat(summary.deckId()).isEqualTo(deck.getId());
        assertThat(summary.totalCards()).isEqualTo(3);
        assertThat(summary.starredCards()).isEqualTo(1);
        assertThat(summary.dueSrsCards()).isEqualTo(2);
        assertThat(summary.newCards()).isEqualTo(2);
        assertThat(summary.learningCards()).isEqualTo(1);
        assertThat(summary.reviewCards()).isZero();
        assertThat(summary.availableModes())
            .contains("FLASHCARDS", "LEARN", "MATCH", "PRACTICE_TEST", "SPACED_REPETITION");
    }
}
