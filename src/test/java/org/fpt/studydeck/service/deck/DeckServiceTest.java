package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.deck.DeckVisibility;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FolderService.class})
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Test
    void createsDeckWithoutFolder() {
        var deck = deckService.createDeck(null, "  Korean Basics  ", "Starter words");

        assertThat(deck.getFolder()).isNull();
        assertThat(deck.getTitle()).isEqualTo("Korean Basics");
        assertThat(deck.getVisibility()).isEqualTo(DeckVisibility.PRIVATE);
    }

    @Test
    void createsDeckInsideFolder() {
        var folder = folderService.createFolder("Languages", null);

        var deck = deckService.createDeck(folder.getId(), "Korean Basics", null);

        assertThat(deck.getFolder().getId()).isEqualTo(folder.getId());
        assertThat(deckRepository.findById(deck.getId())).isPresent();
    }

    @Test
    void rejectsBlankDeckTitle() {
        assertThatThrownBy(() -> deckService.createDeck(null, " ", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Deck title is required.");
    }

    @Test
    void deletesDeckWithFlashcards() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardRepository.save(org.fpt.studydeck.domain.deck.Flashcard.create(
            deck,
            "현장",
            "site",
            null,
            null,
            0
        ));

        deckService.deleteDeck(deck.getId());

        assertThat(deckRepository.findById(deck.getId())).isEmpty();
        assertThat(flashcardRepository.countByDeckId(deck.getId())).isZero();
    }
}
