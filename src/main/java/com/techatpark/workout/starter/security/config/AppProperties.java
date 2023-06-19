package com.techatpark.workout.starter.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * The type App properties.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * declaring an Auth variable final.
     */
    private final Auth auth = new Auth();
    /**
     * declaring an OAuth2 variable final.
     */
    private final OAuth2 oauth2 = new OAuth2();

    /**
     * Gets auth.
     *
     * @return the auth
     */
    public Auth getAuth() {
        return auth;
    }

    /**
     * Gets oauth 2.
     *
     * @return the oauth 2
     */
    public OAuth2 getOauth2() {
        return oauth2;
    }

    /**
     * The type Auth.
     */
    public static class Auth {
        /**
         * declaring variable tokenSecret.
         */
        private String tokenSecret;
        /**
         * declaring variable tokenExpirationMsec.
         */
        private long tokenExpirationMsec;

        /**
         * Gets token secret.
         *
         * @return the token secret
         */
        public String getTokenSecret() {
            return tokenSecret;
        }

        /**
         * Sets token secret.
         *
         * @param atokenSecret the token secret
         */
        public void setTokenSecret(final String atokenSecret) {
            this.tokenSecret = atokenSecret;
        }

        /**
         * Gets token expiration msec.
         *
         * @return the token expiration msec
         */
        public long getTokenExpirationMsec() {
            return tokenExpirationMsec;
        }

        /**
         * Sets token expiration msec.
         *
         * @param atokenExpirationMsec the token expiration msec
         */
        public void setTokenExpirationMsec(final long atokenExpirationMsec) {
            this.tokenExpirationMsec = atokenExpirationMsec;
        }
    }

    /**
     * The type O auth 2.
     */
    public static final class OAuth2 {
        /**
         * declaring a List<String>.
         */
        private List<String> authorizedRedirectUris = new ArrayList<>();

        /**
         * Gets authorized redirect uris.
         *
         * @return the authorized redirect uris
         */
        public List<String> getAuthorizedRedirectUris() {
            return authorizedRedirectUris;
        }

        /**
         * Authorized redirect uris o auth 2.
         *
         * @param anauthorizedRedirectUris the authorized redirect uris
         * @return the o auth 2
         */
        public OAuth2
        authorizedRedirectUris(final
                               List<String>
                                       anauthorizedRedirectUris) {
            this.authorizedRedirectUris = anauthorizedRedirectUris;
            return this;
        }
    }
}
