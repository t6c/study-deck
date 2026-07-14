package org.fpt.studydeck.repository.sorting;

import org.fpt.studydeck.domain.sorting.SortingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SortingSessionRepository extends JpaRepository<SortingSession, Long> {

    @Modifying
    @Query("delete from SortingSessionItem item where item.flashcard.id = :flashcardId")
    int deleteItemsByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Query("delete from SortingSessionItem item where item.flashcard.deck.id = :deckId")
    int deleteItemsByFlashcardDeckId(@Param("deckId") Long deckId);

    @Modifying
    @Query("delete from SortingSession session where session.deck.id = :deckId")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
