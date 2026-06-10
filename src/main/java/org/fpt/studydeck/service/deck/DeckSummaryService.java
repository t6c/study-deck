package org.fpt.studydeck.service.deck;

import java.util.List;

import org.fpt.studydeck.dto.deck.DeckSummaryResponse;
import org.fpt.studydeck.dto.srs.SrsStatsResponse;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.service.srs.SrsStatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeckSummaryService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final List<String> AVAILABLE_MODES = List.of(
        "FLASHCARDS",
        "LEARN",
        "MATCH",
        "PRACTICE_TEST",
        "SPACED_REPETITION"
    );

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final SrsStatsService srsStatsService;

    public DeckSummaryService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        SrsStatsService srsStatsService
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.srsStatsService = srsStatsService;
    }

    public DeckSummaryResponse getSummary(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException(DECK_NOT_FOUND);
        }

        long totalCards = flashcardRepository.countByDeckId(deckId);
        long starredCards = flashcardRepository.countByDeckIdAndStarredTrue(deckId);
        SrsStatsResponse srsStats = srsStatsService.stats(deckId);

        return new DeckSummaryResponse(
            deckId,
            totalCards,
            starredCards,
            srsStats.dueCards(),
            srsStats.newCards(),
            srsStats.learningCards(),
            srsStats.reviewCards(),
            AVAILABLE_MODES
        );
    }
}
