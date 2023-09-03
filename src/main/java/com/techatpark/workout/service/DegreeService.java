package com.techatpark.workout.service;

import com.techatpark.workout.model.Degree;
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
 * The type Degree service.
 */
@Service
public final class DegreeService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(DegreeService.class);


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
    public DegreeService(
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
    private Degree rowMapper(final ResultSet rs,
                                final Integer rowNum)
            throws SQLException {


        Degree degree = new Degree((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return degree;
    }

    /**
     * inserts data.
     *
     * @param userName  the userName
     * @param degree the degree
     * @return question optional
     */
    public Degree create(final String userName,
                            final Degree degree) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("degree")
                .usingColumns("id", "title",
                        "description",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("title",
                degree.title());
        valueMap.put("description", degree.description());
        valueMap.put("created_by", userName);

        final UUID degreeId = UUID.randomUUID();
        valueMap.put("id", degreeId);
        insert.execute(valueMap);
        final Optional<Degree> createdCourse =
                read(userName, degreeId);

        logger.info("Created Degree {}", degreeId);

        return createdCourse.get();
    }


    /**
     * reads from degree.
     *
     * @param id       the id
     * @param userName the userName
     * @return question optional
     */
    public Optional<Degree> read(final String userName, final UUID id) {
        final String query = "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM degree "
                + "WHERE id = ?";


        try {
            final Degree p = jdbcTemplate
                    .queryForObject(query, this::rowMapper, id);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * update the degree.
     *
     * @param id        the id
     * @param userName  the userName
     * @param degree the degree
     * @return question optional
     */
    public Degree update(final UUID id,
                            final String userName,
                            final Degree degree) {
        logger.debug("Entering Update for Degree {}", id);
        final String query =
                "UPDATE degree SET title = ?,"
                        + "description = ?, modified_by = ? WHERE id = ?";
        final Integer updatedRows =
                jdbcTemplate.update(query, degree.title(),
                        degree.description(), userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Degree not found");
        }
        return read(userName, id).get();
    }

    /**
     * delete the degree.
     *
     * @param id       the id
     * @param userName the userName
     * @return false
     */
    public Boolean delete(final String userName, final UUID id) {
        String query = "DELETE FROM degree WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of degree.
     *
     * @param userName the userName
     * @return degree list
     */
    public List<Degree> list(final String userName) {
        String query = "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM degree";
        return jdbcTemplate.query(query, this::rowMapper);
    }

    /**
     * Cleaning up all degree.
     *
     * @return no.of degree deleted
     */
    public Integer deleteAll() {
        final String query = "DELETE FROM degree";
        return jdbcTemplate.update(query);
    }
}
