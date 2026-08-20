package com.autodeploy.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record DomainResponse(
        UUID id,
        UUID projectId,
        String domainName,
        String type,
        String status,
        String sslStatus,
        Boolean verified,
        String verificationToken,
        String cnameTarget,
        Instant createdAt
) {}
