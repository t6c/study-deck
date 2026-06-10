package org.fpt.studydeck.repository.srs;

import org.fpt.studydeck.domain.srs.SrsReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SrsReviewLogRepository extends JpaRepository<SrsReviewLog, Long> {

    void deleteByFlashcardId(Long flashcardId);

    void deleteByFlashcardDeckId(Long deckId);
}
