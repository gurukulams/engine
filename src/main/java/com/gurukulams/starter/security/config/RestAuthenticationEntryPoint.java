package com.gurukulams.starter.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * The type Rest authentication entry point.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        /**
         * declares the logger.
         */
        private static final Logger LOGGER
                = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

        /**
         * commence request.
         *
         * @param httpServletRequest
         * @param httpServletResponse
         * @param e
         * @throws IOException
         */
        @Override
        public void commence(final HttpServletRequest httpServletRequest,
                        final HttpServletResponse httpServletResponse,
                        final AuthenticationException e)
                        throws IOException {
                LOGGER
                .error("Responding with unauthorized error. "
                                + "Url - {}, Message - {}",
                                httpServletRequest.getRequestURL(),
                        e.getMessage());
                httpServletResponse.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                                e.getLocalizedMessage());
        }
}
