package com.gurukulams.starter.security.oauth2.user;

import java.util.Map;

/**
 * The type Github o auth 2 user info.
 */
public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Instantiates a new Github o auth 2 user info.
     *
     * @param theattributes the attributes
     */
    public GithubOAuth2UserInfo(final Map<String, Object> theattributes) {
        super(theattributes);
    }

    /**
     * method getId overrides.
     *
     * @return id
     */
    @Override
    public String getId() {
        return ((Integer) getAttributes().get("id")).toString();
    }

    /**
     * method getName overrides.
     *
     * @return name
     */
    @Override
    public String getName() {
        return (String) getAttributes().get("name");
    }

    /**
     * overrides the method getEmail.
     *
     * @return email
     */
    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    /**
     * overrides method getImageUrl.
     *
     * @return imageurl
     */
    @Override
    public String getImageUrl() {
        return (String) getAttributes().get("avatar_url");
    }
}
