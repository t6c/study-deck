package org.fpt.studydeck.dto.learn;

import jakarta.validation.constraints.PositiveOrZero;

public record CreateLearnSessionRequest(
    @PositiveOrZero(message = "Length of rounds must be zero or positive.")
    int lengthOfRounds,
    boolean flashcards,
    boolean multipleChoice,
    boolean written,
    boolean trueFalse,
    boolean starredOnly,
    boolean shuffleTerms
) {
}
