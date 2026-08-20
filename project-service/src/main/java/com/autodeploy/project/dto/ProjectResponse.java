package com.autodeploy.project.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String slug,
        String repositoryUrl,
        String repositoryName,
        String branch,
        String buildCommand,
        String outputDirectory,
        String dockerfilePath,
        String status,
        List<EnvironmentVariableDto> environmentVariables,
        DeploymentSummaryDto latestDeployment,
        String subdomain,
        String customDomain,
        Instant createdAt,
        Instant updatedAt
) {
    public record DeploymentSummaryDto(
            String id,
            String status,
            String commitSha,
            String commitMessage,
            String branch,
            String url,
            String triggeredBy,
            Integer durationSeconds,
            Instant createdAt
    ) {}
}
