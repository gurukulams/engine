package com.gurukulams.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.DataManager;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.core.service.OrgService;
import com.gurukulams.core.service.ProfileService;
import com.gurukulams.core.service.TagService;
import com.gurukulams.core.service.LearnerProfileService;
import com.gurukulams.core.service.LearnerService;
import com.gurukulams.component.OrgLoader;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class GurukulamsConfig {

    /**
     * Provides Org Service Instalnce.
     * @return orgService
     */
    @Bean
    DataManager dataManager() {
        return DataManager.getManager();
    }

    /**
     * learnerService.
     * @param dataSource
     * @param dataManager
     * @param validator
     * @return learnerService
     */
    @Bean
    LearnerService learnerService(final DataSource dataSource,
                                  final DataManager dataManager,
                                  final Validator validator) {
        return new LearnerService(dataSource,
                dataManager, validator);
    }

    /**
     * learnerProfileService.
     * @param dataSource
     * @param dataManager
     * @param validator
     * @return learnerProfileService
     */
    @Bean
    LearnerProfileService learnerProfileService(
            final DataSource dataSource,
            final DataManager dataManager,
            final Validator validator) {
        return new LearnerProfileService(dataSource,
                dataManager,
                validator);
    }

    /**
     * Builds Profile Service.
     * @param dataSource
     * @param dataManager
     * @param learnerService
     * @param learnerProfileService
     * @param orgService
     * @return profileService
     */
    @Bean
    ProfileService profileService(final DataSource dataSource,
                                  final DataManager dataManager,
                                  final LearnerService learnerService,
          final LearnerProfileService learnerProfileService,
          final OrgService orgService) {
        return new ProfileService(dataSource,
                dataManager,
                learnerService,
                learnerProfileService,
                orgService);
    }

    /**
     * OrgService.
     * @param dataSource
     * @param dataManager
     * @return orgService
     */
    @Bean
    OrgService orgService(final DataSource dataSource,
            final DataManager dataManager) {
        return new OrgService(dataSource, dataManager);
    }

    /**
     * CategoryService.
     * @param dataSource
     * @param dataManager
     * @return categoryService
     */
    @Bean
    CategoryService categoryService(final DataSource dataSource,
            final DataManager dataManager) {
        return new CategoryService(dataSource, dataManager);
    }


    /**
     * TagService.
     * @param dataSource
     * @param dataManager
     * @return tagService
     */
    @Bean
    TagService tagService(final DataSource dataSource,
            final DataManager dataManager) {
        return new TagService(dataSource, dataManager);
    }

    /**
     * OrgLoader.
     * @param objectMapper
     * @param orgService
     * @return orgLoader
     */
    @Bean
    OrgLoader orgLoader(
                        final ObjectMapper objectMapper,
                        final OrgService orgService) {
        return new OrgLoader(orgService, objectMapper);
    }
}
