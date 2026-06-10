package org.fpt.studydeck.exception;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiErrorResponse(
    @Schema(example = "2026-06-10T00:00:00Z")
    String timestamp,

    @Schema(example = "400")
    int status,

    @Schema(example = "Bad Request")
    String error,

    @Schema(example = "Validation failed.")
    String message,

    @Schema(example = "/api/v1/decks")
    String path,

    List<FieldErrorResponse> fieldErrors
) {
}
