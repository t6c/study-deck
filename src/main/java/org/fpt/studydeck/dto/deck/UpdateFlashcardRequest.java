package org.fpt.studydeck.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFlashcardRequest(
    @NotBlank(message = "Term is required.")
    @Size(max = 255)
    String term,

    @NotBlank(message = "Definition is required.")
    @Size(max = 1000)
    String definition,

    @Size(max = 2048)
    String termImageUrl,

    @Size(max = 2048)
    String definitionImageUrl
) {
}
