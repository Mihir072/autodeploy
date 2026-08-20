package com.autodeploy.common.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Base runtime exception for all application-level errors.
 * Carries an HTTP status and a machine-readable error code so that
 * GlobalExceptionHandlers can produce consistent {@link com.autodeploy.common.dto.ErrorResponse}.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected BaseException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    protected BaseException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
