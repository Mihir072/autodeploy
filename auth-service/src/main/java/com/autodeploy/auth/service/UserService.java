package com.autodeploy.auth.service;

import com.autodeploy.auth.dto.UserDto;
import com.autodeploy.auth.entity.User;
import com.autodeploy.auth.repository.UserRepository;
import com.autodeploy.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for user profile lookups.
 * Separate from {@link AuthService} following single-responsibility principle.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Returns the user profile DTO for the given platform UUID.
     * Called by {@code GET /auth/me}.
     *
     * @throws ResourceNotFoundException if no user exists with this ID
     */
    public UserDto findById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserDto.from(user);
    }

    /**
     * Returns the raw entity (for internal use — never return this from a controller).
     *
     * @throws ResourceNotFoundException if no user exists with this ID
     */
    public User findEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
