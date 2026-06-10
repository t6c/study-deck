package org.fpt.studydeck.repository.deck;

import org.fpt.studydeck.domain.deck.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
