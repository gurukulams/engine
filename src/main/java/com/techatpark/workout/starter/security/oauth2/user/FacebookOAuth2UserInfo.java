package com.techatpark.workout.starter.security.oauth2.user;

import java.util.Map;

/**
 * The type Facebook o auth 2 user info.
 */
public class FacebookOAuth2UserInfo extends OAuth2UserInfo {
    /**
     * creating a constructor.
     *
     * @param attributes the attributes
     */
    public FacebookOAuth2UserInfo(final Map<String, Object> attributes) {
        super(attributes);
    }

    /**
     * overrides method.
     *
     * @return id.
     */
    @Override
    public String getId() {
        return (String) getAttributes().get("id");
    }

    /**
     * overrides method.
     *
     * @return name.
     */
    @Override
    public String getName() {
        return (String) getAttributes().get("name");
    }

    /**
     * overrides method.
     *
     * @return email.
     */
    @Override
    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    /**
     * overrides method.
     *
     * @return imageurl.
     */
    @Override
    public String getImageUrl() {
        if (getAttributes().containsKey("picture")) {
            final Map<String, Object> pictureObj =
                    (Map<String, Object>) getAttributes().get("picture");
            if (pictureObj.containsKey("data")) {
                final Map<String, Object> dataObj =
                        (Map<String, Object>) pictureObj.get("data");
                if (dataObj.containsKey("url")) {
                    return (String) dataObj.get("url");
                }
            }
        }
        return null;
    }
}
