package com.autodeploy.project.dto;

import java.util.List;

public record UpdateProjectRequest(
        String name,
        String branch,
        String buildCommand,
        String outputDirectory,
        String dockerfilePath,
        List<EnvironmentVariableDto> environmentVariables
) {}
