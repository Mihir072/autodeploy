package com.autodeploy.auth.dto;

import com.autodeploy.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Public representation of a platform user — never exposes sensitive fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
        String id,
        String username,
        String email,
        String avatarUrl,
        Instant createdAt
) {
    /**
     * Maps a {@link User} entity to a safe DTO.
     * GitHub access tokens and encrypted fields are deliberately excluded.
     */
    public static UserDto from(User user) {
        return new UserDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
