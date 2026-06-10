package org.fpt.studydeck.dto.srs;

public record SrsStatsResponse(
    long newCards,
    long learningCards,
    long reviewCards,
    long dueCards
) {
}
