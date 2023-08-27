package com.techatpark.workout.starter.business;

import com.gurukulams.core.GurukulamsManager;
import com.gurukulams.core.service.OrgService;
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
    OrgService orgService(final DataSource dataSource) {
        return new OrgService(GurukulamsManager.getManager(dataSource));
    }
}
