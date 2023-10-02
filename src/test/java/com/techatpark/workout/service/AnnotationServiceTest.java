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
import java.util.Locale;
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
    void create()  {
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
    void read()  {
        final Optional<Annotation> annotation = annotationService.create("mani", 
                "mani",
                anAnnotation(),
                null,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isPresent(),
                "Annotation Created");

        Assertions.assertTrue(annotationService.read(newAnnotationId, Locale.GERMAN).isEmpty(),
                "Annotation Unavailable for Locale");
    }

    @Test
    void readLocalized()  {
        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                Locale.GERMAN,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isEmpty(),
                "Annotation Unavailable for English");

        Assertions.assertTrue(annotationService.read(newAnnotationId, Locale.GERMAN).isPresent(),
                "Annotation Available for Locale");
    }

    @Test
    void update()  {
        testUpdate(null);
        testUpdate(Locale.GERMAN);
    }

    void testUpdate(Locale locale) {
        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();

        Annotation newAnnotation = new Annotation();
        newAnnotation.setValue(Map.of("a","a","b","b2"));

        Optional<Annotation> updatedAnnotation = annotationService
                .update(newAnnotationId, locale, newAnnotation);
        Assertions.assertEquals("b2", updatedAnnotation.get()
                .getValue().get("b"), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            annotationService
                    .update(UUID.randomUUID(), locale, newAnnotation);
        });
    }

    @Test
    void delete()  {
        testDelete(null);
        testDelete(Locale.GERMAN);
    }

    void testDelete(Locale locale) {
        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        annotationService.delete(annotation.get().getId(), locale);
        Assertions.assertFalse(annotationService.read(annotation.get().getId(), locale).isPresent(),
                "Deleted Annotation");
    }

    @Test
    void list()  {

        testList(null);
        testList(Locale.GERMAN);

    }

    void testList(Locale locale) {
        final Optional<Annotation> annotation = annotationService.create("mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        Annotation newAnnotation = new Annotation();
        newAnnotation.setValue(Map.of("a","a","b","b2"));
        annotationService.create("mani",
                "mani",
                newAnnotation,
                locale,
                "mani");
        List<Annotation> listofannotation = annotationService.list("mani", locale,"mani",
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