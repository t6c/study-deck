package org.fpt.studydeck.service.deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.dto.deck.ViewerCardResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ViewerCardService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String UNSUPPORTED_SORT = "Unsupported card sort.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;

    public ViewerCardService(DeckRepository deckRepository, FlashcardRepository flashcardRepository) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
    }

    public List<ViewerCardResponse> getCards(Long deckId, String sort, String mode) {
        return getFlashcards(deckId, sort).stream()
            .map(ViewerCardResponse::from)
            .toList();
    }

    public List<Flashcard> getFlashcards(Long deckId, String sort) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException(DECK_NOT_FOUND);
        }

        String normalizedSort = normalizeSort(sort);
        if ("original".equals(normalizedSort)) {
            return flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
        }
        if ("starred".equals(normalizedSort)) {
            return flashcardRepository.findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(deckId);
        }
        if ("shuffle".equals(normalizedSort)) {
            List<Flashcard> cards = new ArrayList<>(flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId));
            if (cards.size() > 1) {
                Collections.reverse(cards);
            }
            return cards;
        }

        throw new InvalidRequestException(UNSUPPORTED_SORT);
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "original";
        }
        return sort.trim().toLowerCase();
    }
}
