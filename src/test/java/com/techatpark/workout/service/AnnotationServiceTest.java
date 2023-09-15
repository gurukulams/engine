package com.techatpark.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techatpark.workout.model.Annotation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        final Optional<Annotation> annotation = annotationService.create("mani", 
                "mani",
                anAnnotation(),
                null,
                "mani");
        Assertions.assertTrue(annotationService.read(annotation.get().getId(),
                        null).isPresent(),
                "Created Annotation");
    }

    @Test
    void read() throws JsonProcessingException {
        final Optional<Annotation> annotation = annotationService.create("mani", 
                "mani",
                anAnnotation(),
                null,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isPresent(),
                "Annotation Created");
    }

    @Test
    void update() throws JsonProcessingException {

        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();

        Annotation newAnnotation = new Annotation();
        newAnnotation.setValue(Map.of("a","a","b","b2"));

        Optional<Annotation> updatedAnnotation = annotationService
                .update(newAnnotationId, null, newAnnotation);
        Assertions.assertEquals("b2", updatedAnnotation.get()
                .getValue().get("b"), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            annotationService
                    .update(UUID.randomUUID(), null, newAnnotation);
        });
    }

    @Test
    void delete() throws JsonProcessingException {

        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        annotationService.delete(annotation.get().getId(), null);
        Assertions.assertFalse(annotationService.read(annotation.get().getId(), null).isPresent(),
                "Deleted Annotation");

    }

    @Test
    void list() throws JsonProcessingException {

        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        Annotation newAnnotation = new Annotation();
        newAnnotation.setValue(Map.of("a","a","b","b2"));
        annotationService.create("mani",
                "mani",
                newAnnotation,
                null,
                "mani");
        List<Annotation> listofannotation = annotationService.list("mani", null,"mani",
                "mani"
                );
        Assertions.assertEquals(2, listofannotation.size());

    }


    private Annotation anAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setValue(Map.of("a","a","b","b"));
        return annotation;
    }
    
}