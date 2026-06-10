package org.fpt.studydeck.dto.practice;

import jakarta.validation.constraints.Positive;

public record CreatePracticeTestRequest(
    @Positive(message = "Question count must be positive.")
    int questionCount,
    boolean multipleChoice,
    boolean written,
    boolean trueFalse,
    boolean starredOnly,
    boolean answerWithTerm,
    boolean answerWithDefinition
) {
}
