package com.autodeploy.common.exception;

import org.springframework.http.HttpStatus;

/** Thrown when an authenticated user does not have permission to perform an action (403). */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to perform this action");
    }
}
