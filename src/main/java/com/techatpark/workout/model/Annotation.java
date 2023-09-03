package com.techatpark.workout.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

/**
 * The type User annotation.
 */
public class Annotation {
    /**
     * declare id.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    /**
     * declare text.
     */
    private Map<String, Object> value;


    /**
     * Gets id.
     *
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param anid the id
     */
    public void setId(final UUID anid) {
        this.id = anid;
    }

    /**
     * Gets text.
     *
     * @return the text
     */
    public Map<String, Object> getValue() {
        return value;
    }

    /**
     * Sets text.
     *
     * @param theText the text
     */
    public void setValue(final Map<String, Object> theText) {
        this.value = theText;
    }

}
