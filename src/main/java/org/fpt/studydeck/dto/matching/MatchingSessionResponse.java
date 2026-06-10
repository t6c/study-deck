package org.fpt.studydeck.dto.matching;

import java.util.List;

public record MatchingSessionResponse(
    Long id,
    String status,
    int cardCount,
    int matchedCount,
    long durationMs,
    List<MatchingSessionItemResponse> items
) {
}
