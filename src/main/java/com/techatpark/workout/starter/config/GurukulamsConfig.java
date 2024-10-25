package com.techatpark.workout.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.DataManager;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.core.service.OrgService;
import com.gurukulams.core.service.ProfileService;
import com.gurukulams.core.service.TagService;
import com.gurukulams.notebook.service.AnnotationService;
import com.gurukulams.core.service.LearnerProfileService;
import com.gurukulams.core.service.LearnerService;
import com.techatpark.workout.component.OrgLoader;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class GurukulamsConfig {

    /**
     * Provides Org Service Instalnce.
     * @param dataSource
     * @return orgService
     */
    @Bean
    DataManager dataManager(final DataSource dataSource) {
        return DataManager.getManager(dataSource);
    }

    /**
     * learnerService.
     * @param dataManager
     * @param validator
     * @return learnerService
     */
    @Bean
    LearnerService learnerService(final DataManager dataManager,
                                  final Validator validator) {
        return new LearnerService(dataManager, validator);
    }

    /**
     * learnerProfileService.
     * @param dataManager
     * @param validator
     * @return learnerProfileService
     */
    @Bean
    LearnerProfileService learnerProfileService(
            final DataManager dataManager,
            final Validator validator) {
        return new LearnerProfileService(dataManager,
                validator);
    }

    /**
     * Builds Profile Service.
     * @param dataManager
     * @param learnerService
     * @param learnerProfileService
     * @param orgService
     * @return profileService
     */
    @Bean
    ProfileService profileService(final DataManager dataManager,
                                  final LearnerService learnerService,
          final LearnerProfileService learnerProfileService,
          final OrgService orgService) {
        return new ProfileService(dataManager,
                learnerService,
                learnerProfileService,
                orgService);
    }

    /**
     * OrgService.
     * @param dataManager
     * @return orgService
     */
    @Bean
    OrgService orgService(
            final DataManager dataManager) {
        return new OrgService(dataManager);
    }

    /**
     * CategoryService.
     * @param dataManager
     * @return categoryService
     */
    @Bean
    CategoryService categoryService(
            final DataManager dataManager) {
        return new CategoryService(dataManager);
    }


    /**
     * TagService.
     * @param dataManager
     * @return tagService
     */
    @Bean
    TagService tagService(
            final DataManager dataManager) {
        return new TagService(dataManager);
    }

    /**
     * AnnotationService.
     * @return annotationService
     */
    @Bean
    AnnotationService annotationService() {
        return new AnnotationService();
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
