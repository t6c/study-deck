package org.fpt.studydeck.dto.matching;

import org.fpt.studydeck.domain.matching.MatchingSessionItem;

public record MatchingSessionItemResponse(
    Long id,
    Long flashcardId,
    String term,
    String definition,
    boolean matched
) {

    public static MatchingSessionItemResponse from(MatchingSessionItem item) {
        var flashcard = item.getFlashcard();
        return new MatchingSessionItemResponse(
            item.getId(),
            flashcard.getId(),
            flashcard.getTerm(),
            flashcard.getDefinition(),
            item.isMatched()
        );
    }
}
