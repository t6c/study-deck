package org.fpt.studydeck.dto.srs;

import org.fpt.studydeck.domain.srs.SrsRating;

import jakarta.validation.constraints.NotNull;

public record SrsReviewRequest(
    @NotNull(message = "Rating is required.")
    SrsRating rating,
    long durationMs
) {
}
