package org.fpt.studydeck.repository.matching;

import java.util.Optional;

import org.fpt.studydeck.domain.matching.MatchingSession;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MatchingSessionRepository extends JpaRepository<MatchingSession, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from MatchingSession session where session.id = :id")
    Optional<MatchingSession> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select count(item)
        from MatchingSessionItem item
        where item.session.id = :sessionId and item.matched = false
        """)
    long countUnmatchedItemsBySessionId(@Param("sessionId") Long sessionId);

    @Modifying
    @Query("delete from MatchingSessionItem item where item.flashcard.id = :flashcardId")
    int deleteItemsByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Query("delete from MatchingSessionItem item where item.flashcard.deck.id = :deckId")
    int deleteItemsByFlashcardDeckId(@Param("deckId") Long deckId);

    @Modifying
    @Query("delete from MatchingSession session where session.deck.id = :deckId")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
