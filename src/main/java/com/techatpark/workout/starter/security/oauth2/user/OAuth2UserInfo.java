package com.techatpark.workout.starter.security.oauth2.user;

import java.util.Map;

/**
 * The type O auth 2 user info.
 */
public abstract class OAuth2UserInfo {
    /**
     * The Attributes.
     */
    private Map<String, Object> attributes;


    /**
     * Instantiates a new O auth 2 user info.
     *
     * @param theattributes the attributes
     */
    public OAuth2UserInfo(final Map<String, Object> theattributes) {
        this.attributes = theattributes;
    }

    /**
     * Gets getAttributes().
     *
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public abstract String getId();

    /**
     * Gets name.
     *
     * @return the name
     */
    public abstract String getName();

    /**
     * Gets email.
     *
     * @return the email
     */
    public abstract String getEmail();

    /**
     * Gets image url.
     *
     * @return the image url
     */
    public abstract String getImageUrl();
}
