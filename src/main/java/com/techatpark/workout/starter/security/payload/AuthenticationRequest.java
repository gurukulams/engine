package com.techatpark.workout.starter.security.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The type Authentication request.
 */
public final class AuthenticationRequest {

    /**
     * declares variable.
     */
    private final String userName;
    /**
     * declares variable.
     */
    private final String password;

    /**
     * initializing the userName,password.
     *
     * @param anUserName the an user name
     * @param aPassword  the a password
     */
    @JsonCreator
    public AuthenticationRequest(
            @JsonProperty("userName") final String anUserName,
            @JsonProperty("password") final String aPassword) {
        this.userName = anUserName;
        this.password = aPassword;
    }

    /**
     * gets the value for userName.
     *
     * @return username user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * gets the value for password.
     *
     * @return password password
     */
    public String getPassword() {
        return password;
    }

}
