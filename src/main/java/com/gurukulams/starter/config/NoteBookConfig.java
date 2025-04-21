package com.gurukulams.starter.config;

import com.gurukulams.notebook.service.AnnotationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Note Book.
 */
@Configuration
public class NoteBookConfig {
    /**
     * AnnotationService.
     * @return annotationService
     */
    @Bean
    AnnotationService annotationService() {
        return new AnnotationService();
    }
}
