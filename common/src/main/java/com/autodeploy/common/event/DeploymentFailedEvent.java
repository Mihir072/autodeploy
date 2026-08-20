package com.autodeploy.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * RabbitMQ event published by deployment-service when a deployment fails.
 * Consumed by notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentFailedEvent {

    private String deploymentId;
    private String projectId;
    private String buildId;
    private String userId;
    private String errorMessage;

    /** Whether an automatic rollback was attempted. */
    private boolean rollbackAttempted;

    /** Whether the rollback succeeded (null if rollbackAttempted=false). */
    private Boolean rollbackSucceeded;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
