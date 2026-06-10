package org.fpt.studydeck.service.srs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.srs.SrsCardState;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.domain.srs.SrsState;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, SrsStatsService.class})
class SrsStatsServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private SrsCardStateRepository srsCardStateRepository;

    @Autowired
    private SrsStatsService srsStatsService;

    @Test
    void countsRelearningCardsWithLearningCards() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var relearning = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        srsCardStateRepository.save(reviewedState(relearning, SrsState.RELEARNING));

        var stats = srsStatsService.stats(deck.getId());

        assertThat(stats.learningCards()).isEqualTo(1);
        assertThat(stats.reviewCards()).isZero();
    }

    static SrsCardState reviewedState(Flashcard flashcard, SrsState state) {
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
