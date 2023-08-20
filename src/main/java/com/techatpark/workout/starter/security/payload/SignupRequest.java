package com.techatpark.workout.starter.security.payload;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * SignupRequest.
 */
public class SignupRequest {

    /**
     * email.
     */
    @NotBlank
    @Email
    private String email;

    /**
     * password.
     */
    @NotBlank
    private String password;

    /**
     * imageUrl.
     */
    @NotBlank
    private String imageUrl;
    /**
     * authProvider.
     */
    private AuthProvider authProvider = AuthProvider.local;

    /**
     * getEmail.
     *
     * @return email email
     */
    public String getEmail() {
        return email;
    }

    /**
     * setEmail.
     *
     * @param theemail the theemail
     */
    public void setEmail(final String theemail) {
        this.email = theemail;
    }

    /**
     * getPassword.
     *
     * @return password password
     */
    public String getPassword() {
        return password;
    }

    /**
     * setPassword.
     *
     * @param thepassword the thepassword
     */
    public void setPassword(final String thepassword) {
        this.password = thepassword;
    }

    /**
     * get Image Url.
     *
     * @return imageUrl image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets Image Url.
     *
     * @param aimageUrl the aimage url
     */
    public void setImageUrl(final String aimageUrl) {
        this.imageUrl = aimageUrl;
    }

    /**
     * Gets auth provider.
     *
     * @return the auth provider
     */
    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    /**
     * Sets auth provider.
     *
     * @param paramaAuthProvider the auth provider
     */
    public void setAuthProvider(final AuthProvider paramaAuthProvider) {
        this.authProvider = paramaAuthProvider;
    }
}
