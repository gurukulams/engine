package com.techatpark.workout.service;


import com.gurukulams.core.model.Annotation;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
class AnnotationServiceTest {
    @Autowired
    private AnnotationService annotationService;

    /**
     * Before.
     *
     * @throws SQLException the io exception
     */
    @BeforeEach
    void before() throws SQLException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() throws SQLException {
        cleanUp();
    }

    private void cleanUp() throws SQLException {
        annotationService.delete();
    }

    @Test
    void create() throws SQLException {
        final Annotation annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        Assertions.assertTrue(annotationService.read(annotation.getId(),
                        null).isPresent(),
                "Created Annotation");
    }

    @Test
    void read() throws SQLException {
        final Annotation annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                null,
                "mani");
        final UUID newAnnotationId = annotation.getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isPresent(),
                "Annotation Created");

        Assertions.assertTrue(annotationService.read(newAnnotationId,
                        Locale.GERMAN).isEmpty(),
                "Annotation Unavailable for Locale");
    }

    @Test
    void readLocalized() throws SQLException {
        final Annotation annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                Locale.GERMAN,
                "mani");
        final UUID newAnnotationId = annotation.getId();
        Assertions.assertTrue(annotationService.read(newAnnotationId, null).isEmpty(),
                "Annotation Unavailable for English");

        Assertions.assertTrue(annotationService.read(newAnnotationId,
                        Locale.GERMAN).isPresent(),
                "Annotation Available for Locale");
    }

    @Test
    void list() throws SQLException {

        testList(null);
        testList(Locale.GERMAN);

    }

    void testList(Locale locale) throws SQLException {
        annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        Annotation newAnnotation = new Annotation();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b2");
        newAnnotation.setJsonValue(jsonObject);
        annotationService.create("mani",
                "mani",
                newAnnotation,
                locale,
                "mani");
        List<Annotation> listofannotation = annotationService.list("mani",
                locale, "mani",
                "mani"
        );
        Assertions.assertEquals(2, listofannotation.size());
    }
    @Test
    void update() throws SQLException {
        testUpdate(null);
        testUpdate(Locale.GERMAN);
    }

    void testUpdate(Locale locale) throws SQLException {
        final Annotation annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        final UUID newAnnotationId = annotation.getId();

        Annotation newAnnotation = new Annotation();

        newAnnotation.setId(newAnnotationId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b2");
        newAnnotation.setJsonValue(jsonObject);

        Optional<Annotation> updatedAnnotation = annotationService
                .update(newAnnotationId, locale, newAnnotation);
        Assertions.assertEquals("b2", updatedAnnotation.get()
                .getJsonValue().get("b"), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            annotationService
                    .update(UUID.randomUUID(), locale, newAnnotation);
        });
    }
    @Test
    void delete() throws SQLException {
        testDelete(null);
        testDelete(Locale.GERMAN);
    }

    void testDelete(Locale locale) throws SQLException {
        final Annotation annotation = annotationService.create(
                "mani",
                "mani",
                anAnnotation(),
                locale,
                "mani");
        annotationService.delete(annotation.getId(), locale);
        Assertions.assertFalse(annotationService.read(annotation.getId(), locale).isPresent(),
                "Deleted Annotation");
    }

    private Annotation anAnnotation() {
        Annotation annotation = new Annotation();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "a");
        jsonObject.put("b", "b");
        annotation.setJsonValue(jsonObject);
        return annotation;
    }

}