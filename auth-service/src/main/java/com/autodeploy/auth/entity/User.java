package com.autodeploy.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent user record.
 *
 * <p>Created / updated on every GitHub OAuth2 login.
 * The GitHub access token is stored AES-256-GCM encrypted.</p>
 *
 * <p>Schema owner: auth-service (auth_db.users)</p>
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_github_id", columnList = "github_id"),
        @Index(name = "idx_users_email",     columnList = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** GitHub numeric user ID — the stable, immutable identity key. */
    @Column(name = "github_id", unique = true, nullable = false)
    private Long githubId;

    /** GitHub login name (e.g. "octocat"). May change — we re-sync on login. */
    @Column(name = "username", nullable = false, length = 255)
    private String username;

    /** Primary email. May be null if the user has not shared a public email on GitHub. */
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * AES-256-GCM encrypted GitHub OAuth access token.
     * Decrypted only when the platform needs to call the GitHub API on the user's behalf.
     */
    @Column(name = "github_access_token_encrypted", columnDefinition = "TEXT")
    private String githubAccessTokenEncrypted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /** Convenience constructor for tests and factory methods. */
    public User(Long githubId, String username, String email, String avatarUrl) {
        this.githubId = githubId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }
}
