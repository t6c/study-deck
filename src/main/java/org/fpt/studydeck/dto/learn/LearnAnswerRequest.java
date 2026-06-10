package org.fpt.studydeck.dto.learn;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LearnAnswerRequest(
    @NotNull(message = "Item id is required.")
    Long itemId,

    @NotBlank(message = "Answer is required.")
    String answer
) {
}
