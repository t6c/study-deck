package org.fpt.studydeck.dto.deck;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;

public record FlashcardResponse(
    Long id,
    Long deckId,
    String term,
    String definition,
    String termImageUrl,
    String definitionImageUrl,
    boolean starred,
    int position,
    Instant createdAt,
    Instant updatedAt
) {

    public static FlashcardResponse from(Flashcard flashcard) {
        return new FlashcardResponse(
            flashcard.getId(),
            flashcard.getDeck().getId(),
            flashcard.getTerm(),
            flashcard.getDefinition(),
            flashcard.getTermImageUrl(),
            flashcard.getDefinitionImageUrl(),
            flashcard.isStarred(),
            flashcard.getPosition(),
            flashcard.getCreatedAt(),
            flashcard.getUpdatedAt()
        );
    }
}
