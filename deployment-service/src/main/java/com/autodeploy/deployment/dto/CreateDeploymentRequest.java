package com.autodeploy.deployment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDeploymentRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        UUID buildId,
        String branch,
        String commitSha
) {}
