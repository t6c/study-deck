package org.fpt.studydeck.service.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.learn.LearnSessionRepository;
import org.fpt.studydeck.repository.matching.MatchingSessionRepository;
import org.fpt.studydeck.repository.practice.PracticeTestRepository;
import org.fpt.studydeck.repository.sorting.SortingSessionRepository;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.repository.srs.SrsReviewLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Service
@Transactional
public class FlashcardService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String FLASHCARD_NOT_FOUND = "Flashcard was not found.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final LearnSessionRepository learnSessionRepository;
    private final MatchingSessionRepository matchingSessionRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final SortingSessionRepository sortingSessionRepository;
    private final SrsCardStateRepository srsCardStateRepository;
    private final SrsReviewLogRepository srsReviewLogRepository;
    private final EntityManager entityManager;

    public FlashcardService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        LearnSessionRepository learnSessionRepository,
        MatchingSessionRepository matchingSessionRepository,
        PracticeTestRepository practiceTestRepository,
        SortingSessionRepository sortingSessionRepository,
        SrsCardStateRepository srsCardStateRepository,
        SrsReviewLogRepository srsReviewLogRepository,
        EntityManager entityManager
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.learnSessionRepository = learnSessionRepository;
        this.matchingSessionRepository = matchingSessionRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.sortingSessionRepository = sortingSessionRepository;
        this.srsCardStateRepository = srsCardStateRepository;
        this.srsReviewLogRepository = srsReviewLogRepository;
        this.entityManager = entityManager;
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
        if (!flashcardRepository.existsById(id)) {
            throw new ResourceNotFoundException(FLASHCARD_NOT_FOUND);
        }
        learnSessionRepository.deleteItemsByFlashcardId(id);
        matchingSessionRepository.deleteItemsByFlashcardId(id);
        practiceTestRepository.deleteQuestionsByFlashcardId(id);
        sortingSessionRepository.deleteItemsByFlashcardId(id);
        srsReviewLogRepository.deleteByFlashcardId(id);
        srsCardStateRepository.deleteByFlashcardId(id);
        entityManager.flush();
        entityManager.clear();
        flashcardRepository.deleteById(id);
    }
}
