package com.autodeploy.auth.repository;

import com.autodeploy.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access layer for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their stable GitHub numeric ID.
     * Used during OAuth2 login to detect returning users.
     */
    Optional<User> findByGithubId(Long githubId);

    /**
     * Find a user by their email address.
     * Used for display / notification lookups.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a GitHub ID is already registered.
     */
    boolean existsByGithubId(Long githubId);
}
