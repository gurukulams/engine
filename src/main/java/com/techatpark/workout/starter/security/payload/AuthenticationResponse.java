package com.techatpark.workout.starter.security.payload;

import java.util.List;

/**
 * initializes the value for response.
 *
 * @param userName      the an user name
 * @param displayName the display name
 * @param authToken     the an auth token
 * @param expiresIn       the anExpiresIn
 * @param refreshToken   the a refresh token
 * @param registrationToken the a registration token
 * @param profilePicture the a profile picture
 * @param features the features of the user
 */
public record AuthenticationResponse(
            String userName,
            String displayName,
            String authToken,
            Long expiresIn,
            String refreshToken,
            String registrationToken,
            String profilePicture,
            List<String> features) {
}
