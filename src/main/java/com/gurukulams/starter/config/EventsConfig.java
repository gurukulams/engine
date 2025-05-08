package com.gurukulams.starter.config;

import com.gurukulams.event.DataManager;
import com.gurukulams.event.service.EventService;
import jakarta.validation.Validator;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventsConfig {

    /**
     * EventService.
     * @param aValidator
     * @param url
     * @param username
     * @param password
     * @param flyway
     * @return eventService
     */
    @Bean
    EventService eventService(
            @Value("${spring.gurukulams-events.url}")
            final String url,
            @Value("${spring.datasource.username}")
            final String username,
            @Value("${spring.datasource.password}")
            final String password,
            final Flyway flyway,
            final Validator aValidator) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(username);
        ds.setPassword(password);

        Flyway.configure()
                .configuration(flyway.getConfiguration())
                .dataSource(ds)
                .locations("db/db_upgrades")
                .load()
                .migrate();

        return new EventService(ds, DataManager.getManager(), aValidator);
    }

}
