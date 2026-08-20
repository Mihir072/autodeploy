package com.autodeploy.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * RabbitMQ event published by build-service when a Kaniko build succeeds.
 * Consumed by deployment-service and notification-service.
 *
 * <p>Routing: {@code autodeploy.build.exchange} → key {@code build.completed}
 * → queues {@code autodeploy.build.completed.queue}</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildCompletedEvent {

    private String buildId;
    private String projectId;
    private String userId;

    /** Full ECR image reference: registryUrl/repoName:tag */
    private String imageUri;

    /** Short image tag: {projectId}-{commitSha[0..7]} */
    private String imageTag;

    private String commitSha;
    private String branch;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
