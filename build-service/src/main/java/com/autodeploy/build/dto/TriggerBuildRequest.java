package com.autodeploy.build.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TriggerBuildRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        String branch,
        String commitSha,
        String commitMessage
) {}
