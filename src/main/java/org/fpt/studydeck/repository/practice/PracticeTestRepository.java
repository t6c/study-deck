package org.fpt.studydeck.repository.practice;

import org.fpt.studydeck.domain.practice.PracticeTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PracticeTestRepository extends JpaRepository<PracticeTest, Long> {

    @Modifying
    @Query("delete from PracticeTestQuestion question where question.flashcard.id = :flashcardId")
    int deleteQuestionsByFlashcardId(@Param("flashcardId") Long flashcardId);

    @Modifying
    @Query("delete from PracticeTestQuestion question where question.flashcard.deck.id = :deckId")
    int deleteQuestionsByFlashcardDeckId(@Param("deckId") Long deckId);

    @Modifying
    @Query("delete from PracticeTest test where test.deck.id = :deckId")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
