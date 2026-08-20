package com.autodeploy.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a resource already exists and cannot be created again (409). */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.CONFLICT, "CONFLICT",
              String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
