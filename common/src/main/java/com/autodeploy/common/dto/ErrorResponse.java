package com.autodeploy.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response envelope returned by all services on failure.
 *
 * <pre>
 * {
 *   "success": false,
 *   "error": "RESOURCE_NOT_FOUND",
 *   "message": "Project with id abc123 not found",
 *   "path": "/api/projects/abc123",
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "validationErrors": { "field": "message" }   // optional
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private final boolean success = false;

    private final String error;        // Machine-readable error code (e.g. "RESOURCE_NOT_FOUND")
    private final String message;      // Human-readable description
    private final String path;         // Request path
    private final Instant timestamp;   // When the error occurred

    /** Field-level validation errors: fieldName → error message */
    private final Map<String, List<String>> validationErrors;

    public static ErrorResponse of(String error, String message, String path) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse withValidation(String message, String path,
                                               Map<String, List<String>> validationErrors) {
        return ErrorResponse.builder()
                .error("VALIDATION_FAILED")
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .validationErrors(validationErrors)
                .build();
    }
}
