package org.fpt.studydeck.dto.deck;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Folder;

public record FolderResponse(
    Long id,
    String name,
    String description,
    int position,
    Instant createdAt,
    Instant updatedAt
) {

    public static FolderResponse from(Folder folder) {
        return new FolderResponse(
            folder.getId(),
            folder.getName(),
            folder.getDescription(),
            folder.getPosition(),
            folder.getCreatedAt(),
            folder.getUpdatedAt()
        );
    }
}
