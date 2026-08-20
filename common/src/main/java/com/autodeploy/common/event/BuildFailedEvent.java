package com.autodeploy.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * RabbitMQ event published by build-service when a Kaniko build fails.
 * Consumed by notification-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildFailedEvent {

    private String buildId;
    private String projectId;
    private String userId;
    private String commitSha;
    private String branch;
    private String errorMessage;

    @Builder.Default
    private Instant occurredAt = Instant.now();
}
