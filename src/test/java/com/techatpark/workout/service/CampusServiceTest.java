package com.techatpark.workout.service;

import com.gurukulams.core.model.Campuses;
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
        final Campuses campus = campusService.create("hari",
                aCampus());
        Assertions.assertTrue(campusService.read("hari", campus.getId()).isPresent(), "Created Campuses");
    }

    @Test
    void read() {
        final Campuses campus = campusService.create("hari",
                aCampus());
        final UUID newCourseId = campus.getId();
        Assertions.assertTrue(campusService.read("hari", campus.getId()).isPresent(),
                "Created Campuses");
    }

    @Test
    void update() {

        final Campuses campus = campusService.create("hari",
                aCampus());
        final UUID newCourseId = campus.getId();
        Campuses newCourse = new Campuses();
        newCourse.setTitle("HansiCourse");
        newCourse.setDescription("An Campuses");
        Campuses updatedCourse = campusService
                .update(newCourseId, "priya", newCourse);
        Assertions.assertEquals("HansiCourse", updatedCourse.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            campusService
                    .update(UUID.randomUUID(), "priya", newCourse);
        });
    }

    @Test
    void delete() {

        final Campuses campus = campusService.create("hari",
                aCampus());
        campusService.delete("mani", campus.getId());
        Assertions.assertFalse(campusService.read("mani", campus.getId()).isPresent(), "Deleted Campuses");
    }

    @Test
    void list() {

        final Campuses campus = campusService.create("hari",
                aCampus());
        Campuses newCourse = new Campuses();
        newCourse.setTitle("HansiCourse");
        newCourse.setDescription("An Campuses");
        campusService.create("hari",
                newCourse);
        List<Campuses> listofCourses = campusService.list("hari");
        Assertions.assertEquals(2, listofCourses.size());

    }

    /**
     * Gets practice.
     *
     * @return the practice
     */
    Campuses aCampus() {
        Campuses campus = new Campuses();
        campus.setTitle("HariCourse");
        campus.setDescription("An Campuses");
        return campus;
    }


}