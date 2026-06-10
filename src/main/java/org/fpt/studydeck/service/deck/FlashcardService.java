package org.fpt.studydeck.service.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FlashcardService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String FLASHCARD_NOT_FOUND = "Flashcard was not found.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;

    public FlashcardService(DeckRepository deckRepository, FlashcardRepository flashcardRepository) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public Flashcard createFlashcard(
        Long deckId,
        String term,
        String definition,
        String termImageUrl,
        String definitionImageUrl
    ) {
        Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        int position = Math.toIntExact(flashcardRepository.countByDeckId(deckId));
        return flashcardRepository.save(
            Flashcard.create(deck, term, definition, termImageUrl, definitionImageUrl, position)
        );
    }

    @Transactional(readOnly = true)
    public Flashcard getFlashcard(Long id) {
        return flashcardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(FLASHCARD_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Flashcard> listDeckFlashcards(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException(DECK_NOT_FOUND);
        }
        return flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
    }

    public Flashcard updateFlashcard(
        Long id,
        String term,
        String definition,
        String termImageUrl,
        String definitionImageUrl
    ) {
        Flashcard flashcard = getFlashcard(id);
        flashcard.update(term, definition, termImageUrl, definitionImageUrl);
        return flashcard;
    }

    public Flashcard setStarred(Long id, boolean starred) {
        Flashcard flashcard = getFlashcard(id);
        flashcard.setStarred(starred);
        return flashcard;
    }

    public void deleteFlashcard(Long id) {
        Flashcard flashcard = getFlashcard(id);
        flashcardRepository.delete(flashcard);
    }
}
