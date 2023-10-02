package com.techatpark.workout.service;

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
        final Course course = courseService.create("hari",
                anCourse());
        Assertions.assertTrue(courseService.read("hari", course.id()).isPresent(), "Created Course");
    }

    @Test
    void read() {
        final Course course = courseService.create("hari",
                anCourse());
        final UUID newCourseId = course.id();
        Assertions.assertTrue(courseService.read("hari", course.id()).isPresent(),
                "Created Course");
    }

    @Test
    void update() {

        final Course course = courseService.create("hari",
                anCourse());
        final UUID newCourseId = course.id();
        Course newCourse = new Course(null, "HansiCourse", "An " +
                "Course", null, null, null, null);
        Course updatedCourse = courseService
                .update(newCourseId, "priya", newCourse);
        Assertions.assertEquals("HansiCourse", updatedCourse.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            courseService
                    .update(UUID.randomUUID(), "priya", newCourse);
        });
    }

    @Test
    void delete() {

        final Course course = courseService.create("hari",
                anCourse());
        courseService.delete("mani", course.id());
        Assertions.assertFalse(courseService.read("mani", course.id()).isPresent(), "Deleted Course");
    }

    @Test
    void list() {

        final Course course = courseService.create("hari",
                anCourse());
        Course newCourse = new Course(null, "HansiCourse", "An " +
                "Course", null, null, null, null);
        courseService.create("hari",
                newCourse);
        List<Course> listofCourses = courseService.list("hari");
        Assertions.assertEquals(2, listofCourses.size());

    }

    /**
     * Gets practice.
     *
     * @return the practice
     */
    Course anCourse() {

        Course course = new Course(null, "HariCourse", "An " +
                "Course", null, null, null, null);
        return course;
    }


}