package org.fpt.studydeck.dto.matching;

import jakarta.validation.constraints.Positive;

public record CreateMatchingSessionRequest(
    @Positive(message = "Card count must be positive.")
    int cardCount,
    boolean starredOnly
) {
}
