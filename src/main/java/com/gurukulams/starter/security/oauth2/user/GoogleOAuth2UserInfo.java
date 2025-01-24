package com.gurukulams.starter.security.oauth2.user;

import java.util.Map;

/**
 * The type Google o auth 2 user info.
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    /**
     * Instantiates a new Google o auth 2 user info.
     *
     * @param attributes the attributes
     */
    public GoogleOAuth2UserInfo(final Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * gets the id.
     *
     * @return id
     */
    @Override
    public String getId() {
        return (String) getAttributes().get("sub");
    }

    /**
     * gets the name.
     *
     * @return name
     */
    @Override
    public String getName() {
        return (String) getAttributes().get("name");
    }

    /**
     * gets the emailid.
     *
     * @return email
     */
    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    /**
     * gets the imageurl.
     *
     * @return imageurl
     */
    @Override
    public String getImageUrl() {
        return (String) getAttributes().get("picture");
    }
}
