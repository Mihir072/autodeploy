package com.autodeploy.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * RabbitMQ event published when a user triggers a new build.
 * Consumed by build-service.
 *
 * <p>Routing: {@code autodeploy.build.exchange} → key {@code build.requested}
 * → queue {@code autodeploy.build.requested.queue}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildRequestedEvent {

    private String buildId;
    private String projectId;
    private String userId;

    /** e.g. "owner/repository-name" */
    private String repoFullName;

    /** HTTPS clone URL */
    private String repoUrl;

    private String branch;

    /** Full 40-char commit SHA that triggered the build */
    private String commitSha;

    private String commitMessage;

    /** Optional path to Dockerfile within the repo; null = auto-detect / auto-generate */
    private String dockerfilePath;

    /** Optional build command (e.g. "mvn package -DskipTests") */
    private String buildCommand;

    /** Decrypted env vars for the build — NEVER log these */
    private Map<String, String> envVars;

    /** AWS ECR repository URL (e.g. 123456.dkr.ecr.us-east-1.amazonaws.com/autodeploy) */
    private String ecrRegistryUrl;

    /** Image tag to push: {projectId}-{commitSha[0..7]} */
    private String imageTag;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
