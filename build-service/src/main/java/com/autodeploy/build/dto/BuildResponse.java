package com.autodeploy.build.dto;

import java.time.Instant;
import java.util.UUID;

public record BuildResponse(
        UUID id,
        UUID projectId,
        String status,
        String commitSha,
        String commitMessage,
        String branch,
        String imageUri,
        String imageTag,
        String errorMessage,
        String triggeredBy,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt
) {}
