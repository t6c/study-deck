package org.fpt.studydeck.dto.deck;

import jakarta.validation.constraints.NotNull;

public record StarFlashcardRequest(
    @NotNull(message = "Starred flag is required.")
    Boolean starred
) {
}
