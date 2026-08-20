package com.autodeploy.project.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required")
        String name,

        @NotBlank(message = "Repository URL is required")
        String repositoryUrl,

        @NotBlank(message = "Repository name is required")
        String repositoryName,

        String branch,
        String buildCommand,
        String outputDirectory,
        String dockerfilePath,
        List<EnvironmentVariableDto> environmentVariables
) {}
