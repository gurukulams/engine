package com.techatpark.workout.starter.business;

import com.gurukulams.core.GurukulamsManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class BusinessConfig {
    /**
     * Provides Org Service Instalnce.
     * @param dataSource
     * @return orgService
     */
    @Bean
    GurukulamsManager gurukulamsManager(final DataSource dataSource) {
        return GurukulamsManager.getManager(dataSource);
    }
}
