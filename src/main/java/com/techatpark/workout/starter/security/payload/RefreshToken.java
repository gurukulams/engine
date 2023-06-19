package com.techatpark.workout.starter.security.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The type Authentication request.
 */
public final class RefreshToken {

    /**
     * declares token.
     */
    private final String token;

    /**
     * initializing the userName,password.
     *
     * @param aToken the an user name\
     */
    @JsonCreator
    public RefreshToken(
            @JsonProperty("token") final String aToken) {
        this.token = aToken;
    }

    /**
     * gets the value for userName.
     *
     * @return username user name
     */
    public String getToken() {
        return token;
    }


}
