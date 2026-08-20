package com.autodeploy.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddDomainRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotBlank(message = "Domain name is required")
        String domainName
) {}
