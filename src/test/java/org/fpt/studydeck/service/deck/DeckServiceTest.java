package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.deck.DeckVisibility;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.dto.learn.CreateLearnSessionRequest;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.repository.srs.SrsReviewLogRepository;
import org.fpt.studydeck.service.srs.FsrsScheduler;
import org.fpt.studydeck.service.srs.SrsReviewService;
import org.fpt.studydeck.service.learn.LearnSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeckService.class, FolderService.class, FlashcardService.class, FsrsScheduler.class, SrsReviewService.class, LearnSessionService.class})
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private SrsReviewService srsReviewService;

    @Autowired
    private LearnSessionService learnSessionService;

    @Autowired
    private SrsCardStateRepository srsCardStateRepository;

    @Autowired
    private SrsReviewLogRepository srsReviewLogRepository;

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
    void throwsWhenRemovingDeckFromMissingFolder() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        assertThatThrownBy(() -> deckService.removeDeckFromFolder(999L, deck.getId()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Folder was not found.");
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

    @Test
    void deletesDeckWithReviewedFlashcardsAndSrsData() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        srsReviewService.review(flashcard.getId(), new SrsReviewRequest(SrsRating.GOOD, 1200));

        deckService.deleteDeck(deck.getId());

        assertThat(deckRepository.findById(deck.getId())).isEmpty();
        assertThat(flashcardRepository.countByDeckId(deck.getId())).isZero();
        assertThat(srsCardStateRepository.countByFlashcardDeckId(deck.getId())).isZero();
        assertThat(srsReviewLogRepository.count()).isZero();
    }

    @Test
    void deletesDeckWithFlashcardsReferencedByLearnSession() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        learnSessionService.createSession(
            deck.getId(),
            new CreateLearnSessionRequest(0, true, false, false, false, false, false)
        );

        deckService.deleteDeck(deck.getId());

        assertThat(deckRepository.findById(deck.getId())).isEmpty();
        assertThat(flashcardRepository.countByDeckId(deck.getId())).isZero();
    }
}
