package org.fpt.studydeck.service.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
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
public class DeckService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String FOLDER_NOT_FOUND = "Folder was not found.";

    private final DeckRepository deckRepository;
    private final FolderRepository folderRepository;
    private final FlashcardRepository flashcardRepository;
    private final LearnSessionRepository learnSessionRepository;
    private final MatchingSessionRepository matchingSessionRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final SortingSessionRepository sortingSessionRepository;
    private final SrsCardStateRepository srsCardStateRepository;
    private final SrsReviewLogRepository srsReviewLogRepository;
    private final EntityManager entityManager;

    public DeckService(
        DeckRepository deckRepository,
        FolderRepository folderRepository,
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
        this.folderRepository = folderRepository;
        this.flashcardRepository = flashcardRepository;
        this.learnSessionRepository = learnSessionRepository;
        this.matchingSessionRepository = matchingSessionRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.sortingSessionRepository = sortingSessionRepository;
        this.srsCardStateRepository = srsCardStateRepository;
        this.srsReviewLogRepository = srsReviewLogRepository;
        this.entityManager = entityManager;
    }

    public Deck createDeck(Long folderId, String title, String description) {
        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        }
        return deckRepository.save(Deck.create(folder, title, description));
    }

    @Transactional(readOnly = true)
    public Deck getDeck(Long id) {
        return deckRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Deck> listDecks() {
        return deckRepository.findAll();
    }

    public Deck updateDeck(Long id, String title, String description) {
        Deck deck = getDeck(id);
        deck.update(title, description);
        return deck;
    }

    public Deck moveDeckToFolder(Long folderId, Long deckId) {
        Folder folder = folderRepository.findById(folderId)
            .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        Deck deck = getDeck(deckId);
        deck.moveToFolder(folder);
        return deck;
    }

    public Deck removeDeckFromFolder(Long folderId, Long deckId) {
        if (!folderRepository.existsById(folderId)) {
            throw new ResourceNotFoundException(FOLDER_NOT_FOUND);
        }
        Deck deck = getDeck(deckId);
        if (deck.getFolder() != null && deck.getFolder().getId().equals(folderId)) {
            deck.moveToFolder(null);
        }
        return deck;
    }

    public void deleteDeck(Long id) {
        if (!deckRepository.existsById(id)) {
            throw new ResourceNotFoundException(DECK_NOT_FOUND);
        }
        learnSessionRepository.deleteItemsByFlashcardDeckId(id);
        matchingSessionRepository.deleteItemsByFlashcardDeckId(id);
        practiceTestRepository.deleteQuestionsByFlashcardDeckId(id);
        sortingSessionRepository.deleteItemsByFlashcardDeckId(id);
        learnSessionRepository.deleteByDeckId(id);
        matchingSessionRepository.deleteByDeckId(id);
        practiceTestRepository.deleteByDeckId(id);
        sortingSessionRepository.deleteByDeckId(id);
        srsReviewLogRepository.deleteByFlashcardDeckId(id);
        srsCardStateRepository.deleteByFlashcardDeckId(id);
        flashcardRepository.deleteByDeckId(id);
        entityManager.flush();
        entityManager.clear();
        deckRepository.deleteById(id);
    }
}
