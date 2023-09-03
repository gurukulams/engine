package com.techatpark.workout.service;

import com.techatpark.workout.model.Campus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class CampusServiceTest {

    @Autowired
    private CampusService campusService;

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
        campusService.deleteAll();
    }


    @Test
    void create() {
        final Campus campus = campusService.create("hari",
                aCampus());
        Assertions.assertTrue(campusService.read("hari", campus.id()).isPresent(), "Created Campus");
    }

    @Test
    void read() {
        final Campus campus = campusService.create("hari",
                aCampus());
        final UUID newCourseId = campus.id();
        Assertions.assertTrue(campusService.read("hari", campus.id()).isPresent(),
                "Created Campus");
    }

    @Test
    void update() {

        final Campus campus = campusService.create("hari",
                aCampus());
        final UUID newCourseId = campus.id();
        Campus newCourse = new Campus(null, "HansiCourse", "An " +
                "Campus", null, null, null, null);
        Campus updatedCourse = campusService
                .update(newCourseId, "priya", newCourse);
        Assertions.assertEquals("HansiCourse", updatedCourse.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            campusService
                    .update(UUID.randomUUID(), "priya", newCourse);
        });
    }

    @Test
    void delete() {

        final Campus campus = campusService.create("hari",
                aCampus());
        campusService.delete("mani", campus.id());
        Assertions.assertFalse(campusService.read("mani", campus.id()).isPresent(), "Deleted Campus");
    }

    @Test
    void list() {

        final Campus campus = campusService.create("hari",
                aCampus());
        Campus newCourse = new Campus(null, "HansiCourse", "An " +
                "Campus", null, null, null, null);
        campusService.create("hari",
                newCourse);
        List<Campus> listofCourses = campusService.list("hari");
        Assertions.assertEquals(2, listofCourses.size());

    }

    /**
     * Gets practice.
     *
     * @return the practice
     */
    Campus aCampus() {

        Campus campus = new Campus(null, "HariCourse", "An " +
                "Campus", null, null, null, null);
        return campus;
    }


}