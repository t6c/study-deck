package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, DeckSummaryService.class})
class DeckSummaryServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private DeckSummaryService deckSummaryService;

    @Test
    void returnsDeckSummaryWithCardCountsAndAvailableModes() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        var starred = flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        flashcardService.setStarred(starred.getId(), true);
        flashcardService.createFlashcard(deck.getId(), "third", "three", null, null);

        var summary = deckSummaryService.getSummary(deck.getId());

        assertThat(summary.deckId()).isEqualTo(deck.getId());
        assertThat(summary.totalCards()).isEqualTo(3);
        assertThat(summary.starredCards()).isEqualTo(1);
        assertThat(summary.dueSrsCards()).isZero();
        assertThat(summary.newCards()).isEqualTo(3);
        assertThat(summary.learningCards()).isZero();
        assertThat(summary.reviewCards()).isZero();
        assertThat(summary.availableModes())
            .contains("FLASHCARDS", "LEARN", "MATCH", "PRACTICE_TEST", "SPACED_REPETITION");
    }
}
