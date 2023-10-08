package com.techatpark.workout.service;

import com.gurukulams.core.model.Annotations;
import io.swagger.v3.core.util.Json;
import org.json.JSONObject;
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
    void create() {
        final Optional<Annotations> annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        Assertions.assertTrue(annotationService.read(annotation.get().getId(),
                        null).isPresent(),
                "Created Annotations");
    }

    @Test
    void read() {
        final Optional<Annotations> annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isPresent(),
                "Annotations Created");

        Assertions.assertTrue(annotationService.read(newAnnotationId,
                        Locale.GERMAN).isEmpty(),
                "Annotations Unavailable for Locale");
    }

    @Test
    void readLocalized() {
        final Optional<Annotations> annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                Locale.GERMAN,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isEmpty(),
                "Annotations Unavailable for English");

        Assertions.assertTrue(annotationService.read(newAnnotationId,
                        Locale.GERMAN).isPresent(),
                "Annotations Available for Locale");
    }

    @Test
    void update() {
        testUpdate(null);
        testUpdate(Locale.GERMAN);
    }

    void testUpdate(Locale locale) {
        final Optional<Annotations> annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        final UUID newAnnotationId = annotation.get().getId();

        Annotations newAnnotation = new Annotations();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b2");
        newAnnotation.setJsonValue(jsonObject);

        Optional<Annotations> updatedAnnotation = annotationService
                .update(newAnnotationId, locale, newAnnotation);
        Assertions.assertEquals("b2", updatedAnnotation.get()
                .getJsonValue().get("b"), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            annotationService
                    .update(UUID.randomUUID(), locale, newAnnotation);
        });
    }

    @Test
    void delete() {
        testDelete(null);
        testDelete(Locale.GERMAN);
    }

    void testDelete(Locale locale) {
        final Optional<Annotations> annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        annotationService.delete(annotation.get().getId(), locale);
        Assertions.assertFalse(annotationService.read(annotation.get().getId(), locale).isPresent(),
                "Deleted Annotations");
    }

    @Test
    void list() {

        testList(null);
        testList(Locale.GERMAN);

    }

    void testList(Locale locale) {
        final Optional<Annotations> annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        Annotations newAnnotation = new Annotations();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b2");
        newAnnotation.setJsonValue(jsonObject);
        annotationService.create("mani",
                "mani",
                newAnnotation,
                locale,
                "mani");
        List<Annotations> listofannotation = annotationService.list("mani",
                locale, "mani",
                "mani"
        );
        Assertions.assertEquals(2, listofannotation.size());
    }


    private Annotations anAnnotation() {
        Annotations annotation = new Annotations();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b");
        annotation.setJsonValue(jsonObject);
        return annotation;
    }

}