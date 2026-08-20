package com.autodeploy.deployment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "deployments",
    indexes = {
        @Index(name = "idx_deployments_project_id", columnList = "project_id"),
        @Index(name = "idx_deployments_build_id", columnList = "build_id"),
        @Index(name = "idx_deployments_user_id", columnList = "user_id"),
        @Index(name = "idx_deployments_status", columnList = "status"),
        @Index(name = "idx_deployments_created_at", columnList = "created_at DESC")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "build_id", nullable = false)
    private UUID buildId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "ec2_instance_id")
    private UUID ec2InstanceId;

    @Column(name = "container_id", length = 100)
    private String containerId;

    @Column(name = "image_uri", length = 500)
    private String imageUri;

    @Column(name = "image_tag", length = 255)
    private String imageTag;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "subdomain", length = 255)
    private String subdomain;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "previous_image_tag", length = 255)
    private String previousImageTag;

    @Column(name = "deployed_at")
    private Instant deployedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
