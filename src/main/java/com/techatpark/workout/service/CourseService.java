package com.techatpark.workout.service;

import com.techatpark.workout.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Course service.
 */
@Service
public final class CourseService {

    /**
     * Index.
     */
    private static final int INDEX_1 = 1;
    /**
     * Index.
     */
    private static final int INDEX_2 = 2;
    /**
     * Index.
     */
    private static final int INDEX_3 = 3;
    /**
     * Index.
     */
    private static final int INDEX_4 = 4;
    /**
     * Index.
     */
    private static final int INDEX_5 = 5;
    /**
     * Index.
     */
    private static final int INDEX_6 = 6;
    /**
     * Index.
     */
    private static final int INDEX_7 = 7;

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(CourseService.class);

    /**
     * this helps to execute sql queries.
     */
    private final JdbcClient jdbcClient;

    /**
     * this is the constructor.
     *
     * @param aJdbcClient the JdbcClient
     */
    public CourseService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs     the ResultSet
     * @param rowNum the row number
     * @return course
     * @throws SQLException if a SQL exception occurs
     */
    private Course rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Course course = new Course((UUID) rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDateTime.class),
                rs.getString(INDEX_5),
                rs.getObject(INDEX_6, LocalDateTime.class),
                rs.getString(INDEX_7));
        return course;
    }

    /**
     * Inserts data.
     *
     * @param userName the userName
     * @param course   the course
     * @return created course
     */
    public Course create(final String userName, final Course course) {
        String sql = """
                INSERT INTO courses (id, title, description, created_at,
                created_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        final UUID courseId = UUID.randomUUID();
        jdbcClient.sql(sql)
                .param(INDEX_1, courseId)
                .param(INDEX_2, course.title())
                .param(INDEX_3, course.description())
                .param(INDEX_4, LocalDateTime.now())
                .param(INDEX_5, userName)
                .update();

        final Optional<Course> createdCourse = read(userName, courseId);

        logger.info("Created Course {}", courseId);

        return createdCourse.get();
    }

    /**
     * Reads from course.
     *
     * @param userName the userName
     * @param id       the id
     * @return course optional
     */
    public Optional<Course> read(final String userName, final UUID id) {
        String sql = """
                SELECT id, title, description, created_at, created_by,
                modified_at, modified_by
                FROM courses
                WHERE id = ?
                """;

            return jdbcClient.sql(sql)
                    .param(INDEX_1, id)
                    .query(this::rowMapper)
                    .optional();

    }

    /**
     * Updates the course.
     *
     * @param id       the id
     * @param userName the userName
     * @param course   the course
     * @return updated course
     */
    public Course update(final UUID id, final String userName,
                         final Course course) {
        logger.debug("Entering Update for Course {}", id);
        String sql = """
                UPDATE courses
                SET title = ?, description = ?, modified_by = ?
                WHERE id = ?
                """;
        final Integer updatedRows = jdbcClient.sql(sql)
                .param(INDEX_1, course.title())
                .param(INDEX_2, course.description())
                .param(INDEX_3, userName)
                .param(INDEX_4, id)
                .update();
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Course not found");
        }
        return read(userName, id).get();
    }

    /**
     * Deletes the course.
     *
     * @param userName the userName
     * @param id       the id
     * @return true if deletion is successful, false otherwise
     */
    public Boolean delete(final String userName, final UUID id) {
        String sql = """
                DELETE FROM courses
                WHERE id = ?
                """;
        final Integer updatedRows = jdbcClient.sql(sql)
                .param(INDEX_1, id)
                .update();
        return updatedRows > 0;
    }

    /**
     * Lists all courses.
     *
     * @param userName the userName
     * @return list of courses
     */
    public List<Course> list(final String userName) {
        String sql = """
                SELECT id, title, description, created_at, created_by,
                modified_at, modified_by
                FROM courses
                """;
        return jdbcClient.sql(sql)
                .query(this::rowMapper)
                .list();
    }

    /**
     * Cleans up all courses.
     *
     * @return number of courses deleted
     */
    public Integer deleteAll() {
        String sql = """
                DELETE FROM courses
                """;
        return jdbcClient.sql(sql).update();
    }
}
