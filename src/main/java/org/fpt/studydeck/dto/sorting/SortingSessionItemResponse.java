package org.fpt.studydeck.dto.sorting;

import org.fpt.studydeck.domain.sorting.SortingAnswer;
import org.fpt.studydeck.domain.sorting.SortingSessionItem;

public record SortingSessionItemResponse(
    Long id,
    Long flashcardId,
    String term,
    String definition,
    String termImageUrl,
    String definitionImageUrl,
    boolean starred,
    int position,
    SortingAnswer answer
) {

    public static SortingSessionItemResponse from(SortingSessionItem item) {
        var flashcard = item.getFlashcard();
        return new SortingSessionItemResponse(
            item.getId(),
            flashcard.getId(),
            flashcard.getTerm(),
            flashcard.getDefinition(),
            flashcard.getTermImageUrl(),
            flashcard.getDefinitionImageUrl(),
            flashcard.isStarred(),
            item.getPosition(),
            item.getAnswer()
        );
    }
}
