package org.fpt.studydeck.service.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FolderService {

    private static final String FOLDER_NOT_FOUND = "Folder was not found.";

    private final FolderRepository folderRepository;
    private final DeckRepository deckRepository;

    public FolderService(FolderRepository folderRepository, DeckRepository deckRepository) {
        this.folderRepository = folderRepository;
        this.deckRepository = deckRepository;
    }

    public Folder createFolder(String name, String description) {
        return folderRepository.save(Folder.create(name, description));
    }

    @Transactional(readOnly = true)
    public Folder getFolder(Long id) {
        return folderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Folder> listFolders() {
        return folderRepository.findAll();
    }

    public Folder updateFolder(Long id, String name, String description) {
        Folder folder = getFolder(id);
        folder.rename(name, description);
        return folder;
    }

    public void deleteFolder(Long id) {
        Folder folder = getFolder(id);
        deckRepository.findByFolderId(id).forEach(deck -> deck.moveToFolder(null));
        folderRepository.delete(folder);
    }
}
