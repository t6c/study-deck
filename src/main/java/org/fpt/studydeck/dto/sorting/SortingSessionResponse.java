package org.fpt.studydeck.dto.sorting;

import java.util.List;

public record SortingSessionResponse(
    Long id,
    String status,
    int knownCount,
    int doNotKnowCount,
    List<SortingSessionItemResponse> items
) {
}
