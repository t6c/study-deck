package org.fpt.studydeck.service.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeckService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String FOLDER_NOT_FOUND = "Folder was not found.";

    private final DeckRepository deckRepository;
    private final FolderRepository folderRepository;
    private final FlashcardRepository flashcardRepository;

    public DeckService(
        DeckRepository deckRepository,
        FolderRepository folderRepository,
        FlashcardRepository flashcardRepository
    ) {
        this.deckRepository = deckRepository;
        this.folderRepository = folderRepository;
        this.flashcardRepository = flashcardRepository;
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
        Deck deck = getDeck(id);
        flashcardRepository.deleteAll(flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(id));
        deckRepository.delete(deck);
    }
}
