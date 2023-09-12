package com.techatpark.workout.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * defines database type.
 */
public enum Database {

    /**
     * h2 database.
     */
    H2("h2"),
    /**
     * postgresql database.
     */
    POSTGRES("postgresql");

    /**
     * value will be either mysql or postgres.
     */
    private final String value;

    /**
     * constructor to create type of database.
     *
     * @param aValue
     */
    Database(final String aValue) {
        this.value = aValue;
    }

    /**
     * this method is used to set status from a json.
     *
     * @param value the value
     * @return database database
     */
    @JsonCreator
    public static Database of(final String value) {
        for (final Database database
                : values()) {
            if (value.equals(database.value)) {
                return database;
            }
        }

        return null;
    }

    /**
     * get the value of database.
     *
     * @return value. value
     */
    @JsonValue
    public String getValue() {
        return value;
    }

}
