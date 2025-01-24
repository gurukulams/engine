package com.gurukulams.starter.security.filter;

import com.gurukulams.starter.security.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * The type Token authentication filter.
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    /**
     * N.
     */
    static final int N = 7;
    /**
     * logger.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    /**
     * tokenProvider.
     */
    private final AuthenticationService authenticationService;


    /**
     * TokenAuthenticationFilter.
     *
     * @param aAuthenticationService            token provider
     */
    public TokenAuthenticationFilter(
            final AuthenticationService aAuthenticationService) {
        this.authenticationService = aAuthenticationService;
    }

    /**
     * override method to.
     *
     * @param request     request
     * @param response    response
     * @param filterChain filterchain
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {
        try {
            final String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                UsernamePasswordAuthenticationToken authentication =
                        authenticationService.getAuthentication(
                                request.getRequestURI(), jwt);
                authentication.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(
                                authentication);

            }
        } catch (final Exception ex) {
            LOG.error(
                    "Could not set user authentication in security context",
                    ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(final HttpServletRequest request) {
        final var bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(N);
        }
        return null;
    }
}
