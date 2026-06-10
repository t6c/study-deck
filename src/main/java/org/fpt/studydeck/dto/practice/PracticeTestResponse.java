package org.fpt.studydeck.dto.practice;

import java.util.List;

public record PracticeTestResponse(
    Long id,
    String status,
    int questionCount,
    int answeredCount,
    double scorePercent,
    List<PracticeQuestionResponse> questions
) {
}
