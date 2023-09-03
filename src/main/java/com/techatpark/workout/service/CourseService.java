package com.techatpark.workout.service;

import com.techatpark.workout.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Course service.
 */
@Service
public final class CourseService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(CourseService.class);


    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * this is the connection for the database.
     */
    private final DataSource dataSource;

    /**
     * this is the constructor.
     *
     * @param anJdbcTemplate
     * @param aDataSource
     */
    public CourseService(
            final JdbcTemplate anJdbcTemplate, final DataSource aDataSource) {
        this.jdbcTemplate = anJdbcTemplate;
        this.dataSource = aDataSource;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Course rowMapper(final ResultSet rs,
                                final Integer rowNum)
            throws SQLException {


        Course course = new Course((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return course;
    }

    /**
     * inserts data.
     *
     * @param userName  the userName
     * @param course the course
     * @return question optional
     */
    public Course create(final String userName,
                            final Course course) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("courses")
                .usingColumns("id", "title",
                        "description",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("title",
                course.title());
        valueMap.put("description", course.description());
        valueMap.put("created_by", userName);

        final UUID courseId = UUID.randomUUID();
        valueMap.put("id", courseId);
        insert.execute(valueMap);
        final Optional<Course> createdCourse =
                read(userName, courseId);

        logger.info("Created Course {}", courseId);

        return createdCourse.get();
    }


    /**
     * reads from course.
     *
     * @param id       the id
     * @param userName the userName
     * @return question optional
     */
    public Optional<Course> read(final String userName, final UUID id) {
        final String query = "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM courses "
                + "WHERE id = ?";


        try {
            final Course p = jdbcTemplate
                    .queryForObject(query, this::rowMapper, id);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * update the course.
     *
     * @param id        the id
     * @param userName  the userName
     * @param course the course
     * @return question optional
     */
    public Course update(final UUID id,
                            final String userName,
                            final Course course) {
        logger.debug("Entering Update for Course {}", id);
        final String query =
                "UPDATE courses SET title = ?,"
                        + "description = ?, modified_by = ? WHERE id = ?";
        final Integer updatedRows =
                jdbcTemplate.update(query, course.title(),
                        course.description(), userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Course not found");
        }
        return read(userName, id).get();
    }

    /**
     * delete the course.
     *
     * @param id       the id
     * @param userName the userName
     * @return false
     */
    public Boolean delete(final String userName, final UUID id) {
        String query = "DELETE FROM courses WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of courses.
     *
     * @param userName the userName
     * @return courses list
     */
    public List<Course> list(final String userName) {
        String query = "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM courses";
        return jdbcTemplate.query(query, this::rowMapper);
    }

    /**
     * Cleaning up all courses.
     *
     * @return no.of courses deleted
     */
    public Integer deleteAll() {
        final String query = "DELETE FROM courses";
        return jdbcTemplate.update(query);
    }
}
