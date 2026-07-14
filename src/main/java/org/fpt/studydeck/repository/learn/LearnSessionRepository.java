package org.fpt.studydeck.repository.learn;

import org.fpt.studydeck.domain.learn.LearnSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearnSessionRepository extends JpaRepository<LearnSession, Long> {

    @Modifying
    @Query("delete from LearnSessionItem item where item.flashcard.id = :flashcardId")
    int deleteItemsByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Query("delete from LearnSessionItem item where item.flashcard.deck.id = :deckId")
    int deleteItemsByFlashcardDeckId(@Param("deckId") Long deckId);

    @Modifying
    @Query("delete from LearnSession session where session.deck.id = :deckId")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
