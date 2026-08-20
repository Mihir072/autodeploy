package com.autodeploy.project.dto;

import java.util.List;

public record EnvironmentVariableDto(
        String id,
        String key,
        String value,
        Boolean isSecret
) {}
