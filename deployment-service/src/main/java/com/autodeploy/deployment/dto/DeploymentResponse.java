package com.autodeploy.deployment.dto;

import java.time.Instant;
import java.util.UUID;

public record DeploymentResponse(
        UUID id,
        UUID projectId,
        UUID buildId,
        String status,
        String imageUri,
        String imageTag,
        String subdomain,
        String url,
        String errorMessage,
        String previousImageTag,
        Instant deployedAt,
        Instant createdAt,
        Instant updatedAt
) {}
