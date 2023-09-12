package com.techatpark.workout.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

/**
 * The type Practice.
 */
public class Practice {
    /**
     * tells the id of the exam.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;
    /**
     * tells the title of exam.
     */
    @NotBlank(message = "title is mandatory")
    private String title;

    /**
     * tells the description of exam.
     */
    @NotBlank(message = "description is mandatory")
    private String description;

    /**
     * created_by of exam.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createdBy;

    /**
     * created_by of exam.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant createdAt;


    /**
     * created_by of exam.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant updatedAt;


    /**
     * gets the id of exam.
     *
     * @return id id
     */
    public UUID getId() {
        return id;
    }

    /**
     * sets the id of exam.
     *
     * @param anId the an id
     */
    public void setId(final UUID anId) {
        this.id = anId;
    }

    /**
     * gets the title of exam.
     *
     * @return title title
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the exam title.
     *
     * @param aTitle the a title
     */
    public void setTitle(final String aTitle) {
        this.title = aTitle;
    }

    /**
     * gets description.
     *
     * @return description description
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets description.
     *
     * @param aDescription the a description
     */
    public void setDescription(final String aDescription) {
        this.description = aDescription;
    }

    /**
     * created_by of the exam.
     *
     * @return created_by created_by
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets created_by of the exam.
     *
     * @param theOwner the the created_by
     */
    public void setCreatedBy(final String theOwner) {
        this.createdBy = theOwner;
    }

    /**
     * sets created at.
     *
     * @return createdAt
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * gets created At.
     *
     * @param aCreatedAt the created at
     */
    public void setCreatedAt(final Instant aCreatedAt) {
        this.createdAt = aCreatedAt;
    }

    /**
     * gets updated at.
     *
     * @return updatedAt
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets updated at.
     *
     * @param aupdatedAt
     */
    public void setUpdatedAt(final Instant aupdatedAt) {
        this.updatedAt = aupdatedAt;
    }
}
