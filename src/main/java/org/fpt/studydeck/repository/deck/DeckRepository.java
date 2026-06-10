package org.fpt.studydeck.repository.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findByFolderId(Long folderId);
}
