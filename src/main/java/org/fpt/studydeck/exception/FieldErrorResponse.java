package org.fpt.studydeck.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldErrorResponse(
    @Schema(example = "title")
    String field,

    @Schema(example = "Title is required.")
    String message
) {
}
