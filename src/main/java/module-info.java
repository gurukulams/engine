module gurukulams.engine {

    requires gurukulams.core ;
    requires spring.context;
    requires spring.security.core;
    requires spring.security.config;
    requires spring.security.crypto;
    requires io.swagger.v3.oas.annotations;
    requires spring.web;
    requires org.slf4j;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.security.web;
    requires oauth2.oidc.sdk;
    requires spring.security.oauth2.client;
    requires spring.security.oauth2.core;
    requires io.swagger.v3.oas.models;
    requires spring.core;
    requires jjwt.api;
    requires spring.beans;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j;
    requires spring.webmvc;
    requires flyway.core;
    requires org.apache.tomcat.embed.core;
    requires gurukulams.notebook;
    requires com.fasterxml.jackson.databind;
    requires gurukulams.questionbank;
    requires jakarta.validation;
    requires com.h2database;

    opens com.gurukulams;
    opens com.gurukulams.starter.config;
    opens com.gurukulams.starter.openapi;
    opens com.gurukulams.starter.security.config;
    opens com.gurukulams.starter.security.cache;
    opens com.gurukulams.starter.security.service;
    opens com.gurukulams.starter.security.controller;
    opens com.gurukulams.controller;
    opens com.gurukulams.starter.security.payload;
    opens com.gurukulams.starter.exception;
    opens com.gurukulams.component;

    exports com.gurukulams.component;
    exports com.gurukulams.starter.security.service;
    exports com.gurukulams.starter.security.payload;
    exports com.gurukulams.starter.exception;
}