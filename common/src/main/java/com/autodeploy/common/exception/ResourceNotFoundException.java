package com.autodeploy.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a requested resource does not exist (404). */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
              String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
