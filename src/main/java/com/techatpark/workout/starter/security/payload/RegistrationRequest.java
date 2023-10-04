package com.techatpark.workout.starter.security.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * The type Registration request.
 */
public class RegistrationRequest {


    /**
     * firstName.
     */
    @NotBlank
    private String name;

    /**
     * Date of Birth.
     */
    @Past
    private LocalDate dob;

    /**
     * imageUrl.
     */
    @NotBlank
    @Pattern(regexp = "^[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}$",
            message = "Should be valid Aadhaar number")
    private String aadhar;

    /**
     * getPassword.
     *
     * @return password
     */
    public String getName() {
        return name;
    }

    /**
     * setPassword.
     *
     * @param thepassword
     */
    public void setName(final String thepassword) {
        this.name = thepassword;
    }

    /**
     * get Image Url.
     *
     * @return imageUrl
     */
    public String getAadhar() {
        return aadhar;
    }

    /**
     * Sets Image Url.
     *
     * @param aimageUrl
     */
    public void setAadhar(final String aimageUrl) {
        this.aadhar = aimageUrl;
    }


    /**
     * Gets Dob.
     * @return dob
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * sets Dob.
     * @param aDob
     */
    public void setDob(final LocalDate aDob) {
        this.dob = aDob;
    }
}
