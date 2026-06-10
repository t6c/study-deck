package org.fpt.studydeck.dto.srs;

import java.time.Instant;

import org.fpt.studydeck.domain.srs.SrsState;

public record SrsDueCardResponse(
    Long flashcardId,
    String term,
    String definition,
    Instant dueAt,
    SrsState state
) {
}
