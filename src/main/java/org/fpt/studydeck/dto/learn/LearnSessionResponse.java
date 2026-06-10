package org.fpt.studydeck.dto.learn;

import java.util.List;

public record LearnSessionResponse(
    Long id,
    String status,
    int totalItems,
    int correctCount,
    int wrongCount,
    List<LearnSessionItemResponse> items
) {
}
