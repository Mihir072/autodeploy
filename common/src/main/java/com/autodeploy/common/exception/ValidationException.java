package com.autodeploy.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown for business-rule validation failures (400), distinct from Jakarta Validation errors. */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    public ValidationException(String field, String message) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
              String.format("Validation failed for '%s': %s", field, message));
    }
}
