package org.fpt.studydeck.dto.srs;

import org.fpt.studydeck.domain.srs.SrsRating;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SrsReviewRequest(
    @NotNull(message = "Rating is required.")
    SrsRating rating,
    @PositiveOrZero(message = "Duration must be zero or positive.")
    long durationMs
) {
}
