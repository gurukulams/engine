package com.techatpark.workout.starter.security.oauth2.service;


import com.techatpark.workout.starter.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.techatpark.workout.starter.security.oauth2.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * The type O auth 2 authentication failure handler.
 */
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    /**
     * The Http cookie o auth 2 authorization request repository.
     */
    private final HttpCookieOAuth2AuthorizationRequestRepository
            httpCookieOAuth2AuthorizationRequestRepository;

    /**
     * OAuth2AuthenticationFailureHandler.
     *
     * @param aHttpCookieOAuth2AuthorizationRequestRepository request
     */
    public OAuth2AuthenticationFailureHandler(
            final HttpCookieOAuth2AuthorizationRequestRepository
                    aHttpCookieOAuth2AuthorizationRequestRepository) {
        this.httpCookieOAuth2AuthorizationRequestRepository =
                aHttpCookieOAuth2AuthorizationRequestRepository;
    }

    /**
     * method defines the process on authentication failure.
     *
     * @param request   request
     * @param response  response
     * @param exception exception
     * @throws IOException exception
     */
    @Override
    public void onAuthenticationFailure(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final AuthenticationException exception)
            throws IOException {
        String targetUrl =
                CookieUtils.getCookie(request,
                                HttpCookieOAuth2AuthorizationRequestRepository
                                        .REDIRECT_URI_PARAM_COOKIE_NAME)
                        .map(Cookie::getValue)
                        .orElse(("/"));

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        httpCookieOAuth2AuthorizationRequestRepository
                .removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
