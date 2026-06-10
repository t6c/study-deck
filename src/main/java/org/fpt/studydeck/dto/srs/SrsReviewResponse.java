package org.fpt.studydeck.dto.srs;

import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.domain.srs.SrsState;

public record SrsReviewResponse(
    Long flashcardId,
    SrsRating rating,
    SrsState state,
    Instant dueAt,
    int reps,
    int lapses
) {
}
