package com.autodeploy.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "projects",
    indexes = {
        @Index(name = "idx_projects_user_id", columnList = "user_id"),
        @Index(name = "idx_projects_slug", columnList = "slug"),
        @Index(name = "idx_projects_repo_full_name", columnList = "repo_full_name"),
        @Index(name = "idx_projects_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "repo_full_name", nullable = false, length = 500)
    private String repoFullName;

    @Column(name = "repo_url", nullable = false, length = 500)
    private String repoUrl;

    @Column(name = "branch", nullable = false)
    private String branch = "main";

    @Column(name = "dockerfile_path", length = 500)
    private String dockerfilePath;

    @Column(name = "build_command", length = 1000)
    private String buildCommand;

    @Column(name = "env_vars_encrypted", columnDefinition = "TEXT")
    private String envVarsEncrypted;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "INACTIVE";

    @Column(name = "github_webhook_id")
    private Long githubWebhookId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = "INACTIVE";
        }
        if (branch == null) {
            branch = "main";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
