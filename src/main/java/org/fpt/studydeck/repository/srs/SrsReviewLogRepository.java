package org.fpt.studydeck.repository.srs;

import org.fpt.studydeck.domain.srs.SrsReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SrsReviewLogRepository extends JpaRepository<SrsReviewLog, Long> {

    @Modifying
    @Query("delete from SrsReviewLog log where log.flashcard.id = :flashcardId")
    void deleteByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Query("delete from SrsReviewLog log where log.flashcard.deck.id = :deckId")
    void deleteByFlashcardDeckId(@Param("deckId") Long deckId);
}
