package com.gurukulams.starter.security.oauth2;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.gurukulams.starter.security.oauth2.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;


public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    /**
     * The constant OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.
     */
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME =
            "oauth2_auth_request";
    /**
     * The constant REDIRECT_URI_PARAM_COOKIE_NAME.
     */
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    /**
     * the constant COOKIE_EXPIRE_SECONDS.
     */
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    /**
     * loads the authorisation request.
     *
     * @param request
     * @return OAuth2AuthorizationRequest
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(
            final HttpServletRequest request) {
        return CookieUtils
                .getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils
                        .deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    /**
     * saves the authorizationRequest.
     *
     * @param authorizationRequest
     * @param request
     * @param response
     */
    @Override
    public void saveAuthorizationRequest(
            final OAuth2AuthorizationRequest authorizationRequest,
            final HttpServletRequest request, final HttpServletResponse
                    response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(request, response,
                    OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            CookieUtils.deleteCookie(request, response,
                    REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        CookieUtils
                .addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                        CookieUtils.serialize(authorizationRequest),
                        COOKIE_EXPIRE_SECONDS);
        final String redirectUriAfterLogin =
                request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            CookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }


    /**
     * removeAuthorizationRequest.
     * @param request
     * @param response
     * @return
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            final HttpServletRequest request,
             final HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }


    /**
     * Remove authorization request cookies.
     *
     * @param request  the request
     * @param response the response
     */
    public void removeAuthorizationRequestCookies(final HttpServletRequest
                                                          request,
                                                  final HttpServletResponse
                                                          response) {
        CookieUtils.deleteCookie(request, response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response,
                REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
