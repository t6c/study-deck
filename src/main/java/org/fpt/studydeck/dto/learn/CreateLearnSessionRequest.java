package org.fpt.studydeck.dto.learn;

public record CreateLearnSessionRequest(
    int lengthOfRounds,
    boolean flashcards,
    boolean multipleChoice,
    boolean written,
    boolean trueFalse,
    boolean starredOnly,
    boolean shuffleTerms
) {
}
