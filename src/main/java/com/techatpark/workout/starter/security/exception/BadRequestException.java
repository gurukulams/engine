package com.techatpark.workout.starter.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type Bad request exception.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    /**
     * Basic.
     *
     * @param message the message
     */
    public BadRequestException(final String message) {
        super(message);
    }

    /**
     * With another error.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BadRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
