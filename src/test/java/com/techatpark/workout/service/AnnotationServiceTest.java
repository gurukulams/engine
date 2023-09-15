package com.techatpark.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techatpark.workout.model.Annotation;
import com.techatpark.workout.model.Book;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
class AnnotationServiceTest {
    @Autowired
    private AnnotationService annotationService;

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() {
        cleanUp();
    }

    private void cleanUp() {
        annotationService.deleteAll();
    }

    @Test
    void create() throws JsonProcessingException {
        final Optional<Annotation> annotation = annotationService.create("book",
                "mybook",
                anAnnotation(),
                null,
                "mani");
        Assertions.assertTrue(
                annotationService.read(annotation.get().getId(), null)
                        .isPresent(),
                "Created Book");
    }

    private Annotation anAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setValue(Map.of("a","a","b","b"));
        return annotation;
    }
}