package com.autodeploy.build.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "builds",
    indexes = {
        @Index(name = "idx_builds_project_id", columnList = "project_id"),
        @Index(name = "idx_builds_user_id", columnList = "user_id"),
        @Index(name = "idx_builds_status", columnList = "status"),
        @Index(name = "idx_builds_commit_sha", columnList = "commit_sha"),
        @Index(name = "idx_builds_created_at", columnList = "created_at DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Build {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;

    @Column(name = "commit_message", columnDefinition = "TEXT")
    private String commitMessage;

    @Column(name = "branch", length = 255)
    private String branch;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "QUEUED";

    @Column(name = "image_uri", length = 500)
    private String imageUri;

    @Column(name = "image_tag", length = 255)
    private String imageTag;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "triggered_by", nullable = false, length = 50)
    private String triggeredBy = "MANUAL";

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = "QUEUED";
        }
        if (triggeredBy == null) {
            triggeredBy = "MANUAL";
        }
    }
}
