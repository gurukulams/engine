module gurukulams.engine {
    requires com.fasterxml.jackson.databind;

    requires gurukulams.core ;
    requires spring.context;
    requires spring.security.core;
    requires spring.security.config;
    requires spring.security.crypto;
    requires io.swagger.v3.oas.annotations;
    requires spring.web;
    requires java.sql;
    requires org.slf4j;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires org.apache.tomcat.embed.core;
    requires spring.security.web;
    requires oauth2.oidc.sdk;
    requires spring.security.oauth2.client;
    requires spring.security.oauth2.core;
    requires io.swagger.v3.oas.models;
    requires spring.core;
    requires jjwt.api;
    requires jakarta.validation;
    requires spring.beans;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j;

    opens com.techatpark.workout;
    opens com.techatpark.workout.starter.config;
    opens com.techatpark.workout.starter.openapi;
    opens com.techatpark.workout.starter.security.config;
    opens com.techatpark.workout.starter.security.cache;
    opens com.techatpark.workout.starter.security.service;
    opens com.techatpark.workout.starter.security.controller;
    opens com.techatpark.workout.controller;
    opens com.techatpark.workout.starter.security.payload;

    exports com.techatpark.workout.component.json;
    exports com.techatpark.workout.starter.security.service;
    exports com.techatpark.workout.starter.security.payload;
}