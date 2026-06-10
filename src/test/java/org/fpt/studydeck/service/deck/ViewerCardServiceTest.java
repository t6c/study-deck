package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.fpt.studydeck.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class, ViewerCardService.class})
class ViewerCardServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private ViewerCardService viewerCardService;

    @Test
    void originalReturnsCardsInPositionThenIdOrder() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        flashcardService.createFlashcard(deck.getId(), "third", "three", null, null);

        var cards = viewerCardService.getCards(deck.getId(), "original", "view");

        assertThat(cards).extracting("term").containsExactly("first", "second", "third");
    }

    @Test
    void starredReturnsOnlyStarredCards() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        var starred = flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        flashcardService.setStarred(starred.getId(), true);
        flashcardService.createFlashcard(deck.getId(), "third", "three", null, null);

        var cards = viewerCardService.getCards(deck.getId(), "starred", "view");

        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).term()).isEqualTo("second");
        assertThat(cards.get(0).starred()).isTrue();
    }

    @Test
    void invalidSortThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        assertThatThrownBy(() -> viewerCardService.getCards(deck.getId(), "newest", "view"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage("Unsupported card sort.");
    }

    @Test
    void shuffleReturnsSameCardsWithoutDeterministicReverseOrder() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);
        flashcardService.createFlashcard(deck.getId(), "second", "two", null, null);
        flashcardService.createFlashcard(deck.getId(), "third", "three", null, null);
        flashcardService.createFlashcard(deck.getId(), "fourth", "four", null, null);
        flashcardService.createFlashcard(deck.getId(), "fifth", "five", null, null);

        var shuffled = viewerCardService.getCards(deck.getId(), "shuffle", "view");

        assertThat(shuffled).extracting("term")
            .containsExactlyInAnyOrder("first", "second", "third", "fourth", "fifth");
        assertThat(shuffled.stream().map(card -> card.term()).toList())
            .isNotEqualTo(List.of("first", "second", "third", "fourth", "fifth"))
            .isNotEqualTo(List.of("fifth", "fourth", "third", "second", "first"));
    }
}
