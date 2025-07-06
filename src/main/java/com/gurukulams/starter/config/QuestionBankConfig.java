package com.gurukulams.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurukulams.component.QuestionsLoader;
import com.gurukulams.core.service.CategoryService;
import com.gurukulams.questionbank.DataManager;
import com.gurukulams.questionbank.service.AnswerService;
import com.gurukulams.questionbank.service.QuestionService;
import jakarta.validation.Validator;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Configurations for Question Bank.
 */
@Configuration
public class QuestionBankConfig {
    /**
     * Provides Org Service Instalnce.
     * @param aValidator
     * @param url
     * @param username
     * @param password
     * @param flyway
     * @return questionBankManager
     */
    @Bean
    QuestionService questionService(
            final Validator aValidator,
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


        return new QuestionService(
                aValidator,
                DataManager.getManager(),
                ds);
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
    @Lazy
    QuestionsLoader questionsLoader(final CategoryService categoryService,
                                final ObjectMapper objectMapper,
                                @Value("${app.seed.folder:../gurukulam}")
                                final String seedFolder,
                                final QuestionService questionService) {
        return new QuestionsLoader(categoryService, objectMapper,
                seedFolder, questionService);
    }
}
