package com.autodeploy.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * RabbitMQ event published by deployment-service when a deployment succeeds.
 * Consumed by notification-service (and optionally domain-service).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentCompletedEvent {

    private String deploymentId;
    private String projectId;
    private String buildId;
    private String userId;

    /** The subdomain/full URL where the deployment is now reachable. */
    private String deployedUrl;

    private String imageTag;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
