package com.techatpark.workout.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.questionbank.QuestionBankManager;
import com.gurukulams.questionbank.service.AnswerService;
import com.gurukulams.questionbank.service.QuestionService;
import com.techatpark.workout.component.QuestionsLoader;
import jakarta.validation.Validator;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurations for Question Bank.
 */
@Configuration
public class QuestionBankConfig {
    /**
     * Provides Org Service Instalnce.
     * @param url
     * @param username
     * @param password
     * @param flyway
     * @return questionBankManager
     */
    @Bean
    QuestionBankManager questionBankManager(
            @Value("${spring.question-bank.url}")
            final String url,
            @Value("${spring.datasource.username}")
            final String username,
            @Value("${spring.datasource.password}")
            final String password,
            final Flyway flyway) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(username);
        ds.setPassword(password);

        Flyway.configure()
                .configuration(flyway.getConfiguration())
                .dataSource(ds)
                .locations("db/upgrades")
                .load()
                .migrate();

        return QuestionBankManager.getManager(ds);
    }

    /**
     * QuestionService.
     * @param aValidator
     * @param questionBankManager
     * @return questionService
     */
    @Bean
    QuestionService questionService(final Validator aValidator,
                            final QuestionBankManager questionBankManager) {
        return new QuestionService(
                aValidator,
                questionBankManager);
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
     * @param categoryService
     * @param objectMapper
     * @param seedFolder
     * @param questionService
     * @return questionsLoader
     */
    @Bean
    QuestionsLoader questionsLoader(final CategoryService categoryService,
                        final ObjectMapper objectMapper,
                        @Value("${app.seed.folder:src/test/resources}")
                        final String seedFolder,
                        final QuestionService questionService) {
        return new QuestionsLoader(categoryService, objectMapper,
                seedFolder, questionService);
    }
}
