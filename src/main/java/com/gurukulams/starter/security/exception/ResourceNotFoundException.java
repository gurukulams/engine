package com.gurukulams.starter.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type Resource not found exception.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * declares resourceName.
     */
    private String resourceName;
    /**
     * declares fieldName.
     */
    private String fieldName;
    /**
     * declares fieldValue.
     */
    private Object fieldValue;

    /**
     * Instantiates a new Resource not found exception.
     *
     * @param aresourceName the resource name
     * @param afieldName    the field name
     * @param afieldValue   the field value
     */
    public ResourceNotFoundException(final String aresourceName,
                                     final String afieldName,
                                     final Object afieldValue) {
        super(String
                .format("%s not found with %s : '%s'", aresourceName,
                        afieldName,
                        afieldValue));
        this.resourceName = aresourceName;
        this.fieldName = afieldName;
        this.fieldValue = afieldValue;
    }

    /**
     * Gets resource name.
     *
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Gets field name.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets field value.
     *
     * @return the field value
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}
