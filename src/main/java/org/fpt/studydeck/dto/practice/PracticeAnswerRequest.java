package org.fpt.studydeck.dto.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PracticeAnswerRequest(
    @NotNull(message = "Question id is required.")
    Long questionId,

    @NotBlank(message = "Answer is required.")
    String answer
) {
}
