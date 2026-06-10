package org.fpt.studydeck.dto.matching;

import jakarta.validation.constraints.NotNull;

public record MatchingAnswerRequest(
    @NotNull(message = "Item id is required.")
    Long itemId
) {
}
