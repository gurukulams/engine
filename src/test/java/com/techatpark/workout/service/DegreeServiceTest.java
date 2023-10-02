package com.techatpark.workout.service;

import com.gurukulams.core.model.Degree;
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
class DegreeServiceTest {

    @Autowired
    private DegreeService degreeService;

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
        degreeService.deleteAll();
    }


    @Test
    void create() {
        final Degree degree = degreeService.create("hari",
                aDegree());
        Assertions.assertTrue(degreeService.read("hari", degree.getId()).isPresent(), "Created Degree");
    }

    @Test
    void read() {
        final Degree degree = degreeService.create("hari",
                aDegree());
        final UUID newDegreeId = degree.getId();
        Assertions.assertTrue(degreeService.read("hari", degree.getId()).isPresent(),
                "Created Degree");
    }

    @Test
    void update() {

        final Degree degree = degreeService.create("hari",
                aDegree());
        final UUID newDegreeId = degree.getId();
        Degree newDegree = new Degree();
        newDegree.setTitle("HansiCourse");
        newDegree.setDescription("An Degree");
        Degree updatedDegree = degreeService
                .update(newDegreeId, "priya", newDegree);
        Assertions.assertEquals("HansiCourse", updatedDegree.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            degreeService
                    .update(UUID.randomUUID(), "priya", newDegree);
        });
    }

    @Test
    void delete() {

        final Degree degree = degreeService.create("hari",
                aDegree());
        degreeService.delete("mani", degree.getId());
        Assertions.assertFalse(degreeService.read("mani", degree.getId()).isPresent(), "Deleted Degree");
    }

    @Test
    void list() {

        final Degree degree = degreeService.create("hari",
                aDegree());
        Degree newDegree = new Degree();
        newDegree.setTitle("HansiDegree");
        newDegree.setDescription("An Degree");
        degreeService.create("hari",
                newDegree);
        List<Degree> listofCourses = degreeService.list("hari");
        Assertions.assertEquals(2, listofCourses.size());

    }

    /**
     * Gets practice.
     *
     * @return the practice
     */
    Degree aDegree() {

        Degree degree = new Degree();
        degree.setTitle("HariDegree");
        degree.setDescription("An Degree");
        return degree;
    }


}