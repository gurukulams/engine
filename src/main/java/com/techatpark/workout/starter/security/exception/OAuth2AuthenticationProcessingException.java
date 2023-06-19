package com.techatpark.workout.starter.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * The type O auth 2 authentication processing exception.
 */
public class OAuth2AuthenticationProcessingException
        extends AuthenticationException {
    /**
     * declare a constructor.
     *
     * @param msg the msg
     * @param t   the t
     */
    public OAuth2AuthenticationProcessingException(final String msg,
                                                   final Throwable t) {
        super(msg, t);
    }

    /**
     * Instantiates a new O auth 2 authentication processing exception.
     *
     * @param msg the msg
     */
    public OAuth2AuthenticationProcessingException(final String msg) {
        super(msg);
    }
}
