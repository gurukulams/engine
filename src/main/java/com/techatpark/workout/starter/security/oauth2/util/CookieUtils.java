package com.techatpark.workout.starter.security.oauth2.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Base64;
import java.util.Optional;

/**
 * The type Cookie utils.
 */
public final class CookieUtils {

    /**
     * Instantiates a new Cookie utils.
     */
    private CookieUtils() {

    }

    /**
     * dash.
     *
     * @param request the request
     * @param name    the name
     * @return a. cookie
     */
    public static Optional<Cookie> getCookie(
            final HttpServletRequest request,
            final String name) {
        final Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * ssdsd.
     *
     * @param response the response
     * @param name     the name
     * @param value    the value
     * @param maxAge   the max age
     */
    public static void addCookie(
            final HttpServletResponse response,
            final String name,
            final String value,
            final int maxAge) {
        final Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * dd.
     *
     * @param request  the request
     * @param response the response
     * @param name     the name
     */
    public static void deleteCookie(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (final Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * aa.
     *
     * @param object the object
     * @return nn. string
     */
    public static String serialize(final Serializable object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * aaa.
     *
     * @param <T>    the type parameter
     * @param cookie the cookie
     * @param cls    the cls
     * @return mm. t
     */
    public static <T> T deserialize(final Cookie cookie, final Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.getValue())));
    }


}
