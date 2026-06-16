package org.fpt.studydeck.dto.auth;

import org.fpt.studydeck.domain.auth.AppUser;

public record AuthUserResponse(
    Long id,
    String email,
    String displayName
) {

    public static AuthUserResponse from(AppUser user) {
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
