package org.fpt.studydeck.dto.deck;

import java.util.List;

public record DeckSummaryResponse(
    Long deckId,
    long totalCards,
    long starredCards,
    long dueSrsCards,
    long newCards,
    long learningCards,
    long reviewCards,
    List<String> availableModes
) {
}
