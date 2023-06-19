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
     * getEmail.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * setEmail.
     *
     * @param theemail
     */
    public void setEmail(final String theemail) {
        this.email = theemail;
    }

    /**
     * getPassword.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * setPassword.
     *
     * @param thepassword
     */
    public void setPassword(final String thepassword) {
        this.password = thepassword;
    }

    /**
     * get Image Url.
     *
     * @return imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets Image Url.
     *
     * @param aimageUrl
     */
    public void setImageUrl(final String aimageUrl) {
        this.imageUrl = aimageUrl;
    }
}
