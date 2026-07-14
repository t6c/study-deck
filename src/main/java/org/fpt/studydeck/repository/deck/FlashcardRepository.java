package org.fpt.studydeck.repository.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByDeckIdOrderByPositionAscIdAsc(Long deckId);

    List<Flashcard> findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(Long deckId);

    long countByDeckId(Long deckId);

    long countByDeckIdAndStarredTrue(Long deckId);

    @Modifying
    @Query("delete from Flashcard flashcard where flashcard.deck.id = :deckId")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
