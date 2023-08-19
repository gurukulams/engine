package com.techatpark.workout.starter.security.oauth2;


import com.techatpark.workout.starter.security.config.AppProperties;
import com.techatpark.workout.starter.security.exception.BadRequestException;
import com.techatpark.workout.starter.security.oauth2.util.CookieUtils;
import com.techatpark.workout.starter.security.service.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * The type O auth 2 authentication success handler.
 */
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    /**
     * declare a TokenProvider.
     */
    private final TokenProvider tokenProvider;

    /**
     * declare a AppProperties.
     */
    private final AppProperties appProperties;

    /**
     * declare a HttpCookieOAuth2AuthorizationRequestRepository.
     */
    private final HttpCookieOAuth2AuthorizationRequestRepository
            httpCookieOAuth2AuthorizationRequestRepository;


    /**
     * Instantiates a new O auth 2 authentication success handler.
     *
     * @param atokenProvider                                  the token provider
     * @param theappProperties                                the app properties
     * @param ahttpCookieOAuth2AuthorizationRequestRepository the http
     *                                                        cookie o auth
     *                                                        2 authorization
     *                                                        request repository
     */
    @Autowired
    public OAuth2AuthenticationSuccessHandler(
            final TokenProvider atokenProvider,
            final AppProperties theappProperties,
            final HttpCookieOAuth2AuthorizationRequestRepository
                    ahttpCookieOAuth2AuthorizationRequestRepository) {
        this.tokenProvider = atokenProvider;
        this.appProperties = theappProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository =
                ahttpCookieOAuth2AuthorizationRequestRepository;
    }

    /**
     * overrides the method onAuthenticationSuccess.
     *
     * @param arequest         request
     * @param aresponse        response
     * @param anauthentication authentication
     * @throws IOException exception
     */
    @Override
    public void onAuthenticationSuccess(final HttpServletRequest arequest,
                                        final HttpServletResponse aresponse,
                                        final Authentication anauthentication)
            throws IOException {
        final String targetUrl =
                determineTargetUrl(arequest, aresponse, anauthentication);

        if (aresponse.isCommitted()) {
            logger.debug(
                    "Response has already been committed."
                            + " Unable to redirect to "
                            + targetUrl);
            return;
        }

        clearAuthenticationAttributes(arequest, aresponse);
        getRedirectStrategy().sendRedirect(arequest, aresponse, targetUrl);
    }

    /**
     * determine target url.
     *
     * @param arequest         request
     * @param aresponse        response
     * @param anauthentication authentication
     * @return targeturl target url
     */
    protected String determineTargetUrl(final HttpServletRequest arequest,
                                        final HttpServletResponse aresponse,
                                        final Authentication anauthentication) {
        final Optional<String> redirectUri =
                CookieUtils.getCookie(arequest,
                                HttpCookieOAuth2AuthorizationRequestRepository
                                        .REDIRECT_URI_PARAM_COOKIE_NAME)
                        .map(Cookie::getValue);

        if (redirectUri.isPresent()
                && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException(
                    "Sorry! We've got an Unauthorized Redirect URI and"
                            + " can't proceed with the authentication: "
                            + redirectUri.get());
        }

        final String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        final String token = tokenProvider
                .generateWelcomeToken(anauthentication.getName());

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
    }

    /**
     * Clear authentication getAttributes().
     *
     * @param arequest  the request
     * @param aresponse the response
     */
    protected void clearAuthenticationAttributes(final HttpServletRequest
                                                         arequest,
                                                 final HttpServletResponse
                                                         aresponse) {
        super.clearAuthenticationAttributes(arequest);
        httpCookieOAuth2AuthorizationRequestRepository
                .removeAuthorizationRequestCookies(arequest, aresponse);
    }

    private boolean isAuthorizedRedirectUri(final String uri) {
        final URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use
                    // different paths if they want to
                    final URI authorizedURI =
                            URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost()
                            .equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort()
                            == clientRedirectUri.getPort();
                });
    }
}
