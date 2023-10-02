package com.techatpark.workout.service;

import com.gurukulams.core.model.Courses;
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
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

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
        courseService.deleteAll();
    }


    @Test
    void create() {
        final Courses course = courseService.create("hari",
                anCourse());
        Assertions.assertTrue(courseService.read("hari", course.getId()).isPresent(), "Created Courses");
    }

    @Test
    void read() {
        final Courses course = courseService.create("hari",
                anCourse());
        final UUID newCourseId = course.getId();
        Assertions.assertTrue(courseService.read("hari", course.getId()).isPresent(),
                "Created Courses");
    }

    @Test
    void update() {

        final Courses course = courseService.create("hari",
                anCourse());
        final UUID newCourseId = course.getId();
        Courses newCourse = new Courses();
        newCourse.setTitle("HansiCourse");
        newCourse.setDescription("An Courses");
        Courses updatedCourse = courseService
                .update(newCourseId, "priya", newCourse);
        Assertions.assertEquals("HansiCourse", updatedCourse.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            courseService
                    .update(UUID.randomUUID(), "priya", newCourse);
        });
    }

    @Test
    void delete() {

        final Courses course = courseService.create("hari",
                anCourse());
        courseService.delete("mani", course.getId());
        Assertions.assertFalse(courseService.read("mani", course.getId()).isPresent(), "Deleted Courses");
    }

    @Test
    void list() {

        final Courses course = courseService.create("hari",
                anCourse());
        Courses newCourse = new Courses();
        newCourse.setTitle("HansiCourse");
        newCourse.setDescription("An Courses");
        courseService.create("hari",
                newCourse);
        List<Courses> listofCourses = courseService.list("hari");
        Assertions.assertEquals(2, listofCourses.size());

    }

    /**
     * Gets practice.
     *
     * @return the practice
     */
    Courses anCourse() {

        Courses course = new Courses();
        course.setTitle("HariCourse");
        course.setDescription("An Courses");
        return course;
    }


}