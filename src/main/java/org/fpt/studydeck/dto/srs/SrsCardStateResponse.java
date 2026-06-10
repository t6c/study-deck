package org.fpt.studydeck.dto.srs;

import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsState;

public record SrsCardStateResponse(
    Long flashcardId,
    SrsState state,
    Instant dueAt,
    double stability,
    double difficulty,
    int reps,
    int lapses
) {
}
