package org.fpt.studydeck.service.srs;

import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsState;
import org.fpt.studydeck.dto.srs.SrsStatsResponse;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SrsStatsService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final SrsCardStateRepository srsCardStateRepository;

    public SrsStatsService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        SrsCardStateRepository srsCardStateRepository
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.srsCardStateRepository = srsCardStateRepository;
    }

    public SrsStatsResponse stats(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException(DECK_NOT_FOUND);
        }
        long totalCards = flashcardRepository.countByDeckId(deckId);
        long cardStateCount = srsCardStateRepository.countByFlashcardDeckId(deckId);
        long newCards = totalCards - cardStateCount;
        long learningCards = srsCardStateRepository.countByFlashcardDeckIdAndState(deckId, SrsState.LEARNING);
        long reviewCards = srsCardStateRepository.countByFlashcardDeckIdAndState(deckId, SrsState.REVIEW);
        long dueCards = newCards + srsCardStateRepository.countDueByDeckId(deckId, Instant.now());

        return new SrsStatsResponse(newCards, learningCards, reviewCards, dueCards);
    }
}
