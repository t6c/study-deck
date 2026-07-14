package org.fpt.studydeck.repository.srs;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.fpt.studydeck.domain.srs.SrsCardState;
import org.fpt.studydeck.domain.srs.SrsState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SrsCardStateRepository extends JpaRepository<SrsCardState, Long> {

    Optional<SrsCardState> findByFlashcardId(Long flashcardId);

    @Modifying
    @Query("delete from SrsCardState state where state.flashcard.id = :flashcardId")
    void deleteByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Query("delete from SrsCardState state where state.flashcard.deck.id = :deckId")
    void deleteByFlashcardDeckId(@Param("deckId") Long deckId);

    long countByFlashcardDeckId(Long deckId);

    long countByFlashcardDeckIdAndState(Long deckId, SrsState state);

    @Query("""
        select count(s)
        from SrsCardState s
        where s.flashcard.deck.id = :deckId and s.dueAt <= :now
        """)
    long countDueByDeckId(@Param("deckId") Long deckId, @Param("now") Instant now);

    @Query("""
        select s
        from SrsCardState s
        join fetch s.flashcard f
        where f.deck.id = :deckId and s.dueAt <= :now
        order by f.position asc, f.id asc
        """)
    List<SrsCardState> findDueByDeckId(@Param("deckId") Long deckId, @Param("now") Instant now);
}
