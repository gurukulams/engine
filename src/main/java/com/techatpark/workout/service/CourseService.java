package com.techatpark.workout.service;

import com.gurukulams.core.model.Courses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Courses rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Courses course = new Courses();
        course.setId((UUID) rs.getObject(INDEX_1));
        course.setTitle(rs.getString(INDEX_2));
                course.setDescription(rs.getString(INDEX_3));
                course.setCreatedAt(rs.getObject(INDEX_4, LocalDateTime.class));
                course.setCreatedBy(rs.getString(INDEX_5));
                course.setModifiedAt(rs.getObject(INDEX_6, LocalDateTime.class));
                course.setModifiedBy(rs.getString(INDEX_7));
        return course;
    }

    /**
     * Inserts data.
     *
     * @param userName the userName
     * @param course   the course
     * @return created course
     */
    public Courses create(final String userName, final Courses course) {
        String sql = """
                INSERT INTO courses (id, title, description, created_at,
                created_by)
                VALUES (?, ?, ?, ?, ?)
                """;
        final UUID courseId = UUID.randomUUID();
        jdbcClient.sql(sql)
                .param(INDEX_1, courseId)
                .param(INDEX_2, course.getTitle())
                .param(INDEX_3, course.getDescription())
                .param(INDEX_4, LocalDateTime.now())
                .param(INDEX_5, userName)
                .update();

        final Optional<Courses> createdCourse = read(userName, courseId);

        logger.info("Created Courses {}", courseId);

        return createdCourse.get();
    }

    /**
     * Reads from course.
     *
     * @param userName the userName
     * @param id       the id
     * @return course optional
     */
    public Optional<Courses> read(final String userName, final UUID id) {
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
    public Courses update(final UUID id, final String userName,
                         final Courses course) {
        logger.debug("Entering Update for Courses {}", id);
        String sql = """
                UPDATE courses
                SET title = ?, description = ?, modified_by = ?
                WHERE id = ?
                """;
        final Integer updatedRows = jdbcClient.sql(sql)
                .param(INDEX_1, course.getTitle())
                .param(INDEX_2, course.getDescription())
                .param(INDEX_3, userName)
                .param(INDEX_4, id)
                .update();
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Courses not found");
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
    public List<Courses> list(final String userName) {
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
