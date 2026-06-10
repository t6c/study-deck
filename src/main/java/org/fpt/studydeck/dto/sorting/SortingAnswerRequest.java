package org.fpt.studydeck.dto.sorting;

import org.fpt.studydeck.domain.sorting.SortingAnswer;

import jakarta.validation.constraints.NotNull;

public record SortingAnswerRequest(
    @NotNull(message = "Item id is required.")
    Long itemId,

    @NotNull(message = "Sorting answer is required.")
    SortingAnswer answer
) {
}
