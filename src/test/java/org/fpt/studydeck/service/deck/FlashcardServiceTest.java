package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FlashcardService.class})
class FlashcardServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Test
    void createsFlashcardWithOptionalImageUrls() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        var flashcard = flashcardService.createFlashcard(
            deck.getId(),
            " 현장 ",
            " site ",
            " https://example.com/term.png ",
            " https://example.com/definition.png "
        );

        assertThat(flashcard.getTerm()).isEqualTo("현장");
        assertThat(flashcard.getDefinition()).isEqualTo("site");
        assertThat(flashcard.getTermImageUrl()).isEqualTo("https://example.com/term.png");
        assertThat(flashcard.getDefinitionImageUrl()).isEqualTo("https://example.com/definition.png");
        assertThat(flashcard.isStarred()).isFalse();
        assertThat(flashcard.getPosition()).isZero();
    }

    @Test
    void togglesStarredFlag() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        var starred = flashcardService.setStarred(flashcard.getId(), true);

        assertThat(starred.isStarred()).isTrue();
    }

    @Test
    void rejectsBlankTerm() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        assertThatThrownBy(() -> flashcardService.createFlashcard(deck.getId(), " ", "site", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Term is required.");
    }
}
