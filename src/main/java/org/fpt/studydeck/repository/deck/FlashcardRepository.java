package org.fpt.studydeck.repository.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByDeckIdOrderByPositionAscIdAsc(Long deckId);

    List<Flashcard> findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(Long deckId);

    long countByDeckId(Long deckId);

    long countByDeckIdAndStarredTrue(Long deckId);
}
