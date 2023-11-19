package com.techatpark.workout.starter.security.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

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
            @JsonProperty("userName")  String userName,
            @JsonProperty("displayName")  String displayName,
            @JsonProperty("authToken")  String authToken,
            @JsonProperty("expires_in")  Long expiresIn,
            @JsonProperty("refresh_token")  String refreshToken,
            @JsonProperty("registration_token")  String registrationToken,
            @JsonProperty("profile_pic")  String profilePicture,
            @JsonProperty("features")   List<String> features) {
}
