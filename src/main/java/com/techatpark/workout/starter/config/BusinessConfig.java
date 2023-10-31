package com.techatpark.workout.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.GurukulamsManager;
import com.gurukulams.core.service.AnnotationService;
import com.gurukulams.core.service.AnswerService;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.core.service.LearnerProfileService;
import com.gurukulams.core.service.LearnerService;
import com.gurukulams.core.service.QuestionService;
import com.gurukulams.core.service.TagService;
import com.techatpark.workout.component.QuestionsLoader;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * learnerService.
     * @param gurukulamsManager
     * @param validator
     * @return learnerService
     */
    @Bean
    LearnerService learnerService(final GurukulamsManager gurukulamsManager,
                                  final Validator validator) {
        return new LearnerService(gurukulamsManager, validator);
    }

    /**
     * learnerProfileService.
     * @param gurukulamsManager
     * @return learnerProfileService
     */
    @Bean
    LearnerProfileService learnerProfileService(
            final GurukulamsManager gurukulamsManager) {
        return new LearnerProfileService(gurukulamsManager);
    }

    /**
     * CategoryService.
     * @param gurukulamsManager
     * @return categoryService
     */
    @Bean
    CategoryService categoryService(
            final GurukulamsManager gurukulamsManager) {
        return new CategoryService(gurukulamsManager);
    }


    /**
     * TagService.
     * @param gurukulamsManager
     * @return tagService
     */
    @Bean
    TagService tagService(
            final GurukulamsManager gurukulamsManager) {
        return new TagService(gurukulamsManager);
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
     * QuestionService.
     * @param aCategoryService
     * @param aValidator
     * @param gurukulamsManager
     * @return questionService
     */
    @Bean
    QuestionService questionService(final CategoryService aCategoryService,
                            final Validator aValidator,
                            final GurukulamsManager gurukulamsManager) {
        return new QuestionService(aCategoryService,
                aValidator,
                gurukulamsManager);
    }

    /**
     * AnswerService.
     * @param questionService
     * @return answerService
     */
    @Bean
    AnswerService answerService(final QuestionService questionService) {
        return new AnswerService(questionService);
    }

    /**
     * questionsLoader.
     * @param tagService
     * @param objectMapper
     * @param seedFolder
     * @param questionService
     * @return questionsLoader
     */
    @Bean
    QuestionsLoader questionsLoader(final CategoryService tagService,
                    final ObjectMapper objectMapper,
                    @Value("${app.seed.folder:src/test/resources}")
                    final String seedFolder,
                    final QuestionService questionService) {
        return new QuestionsLoader(tagService, objectMapper,
                seedFolder, questionService);
    }

}
