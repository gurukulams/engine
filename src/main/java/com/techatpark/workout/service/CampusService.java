package com.techatpark.workout.service;

import com.techatpark.workout.model.Campus;
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
 * The type Campus service.
 */
@Service
public final class CampusService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(CampusService.class);


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
    public CampusService(
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
    private Campus rowMapper(final ResultSet rs,
                                final Integer rowNum)
            throws SQLException {


        Campus campus = new Campus((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return campus;
    }

    /**
     * inserts data.
     *
     * @param userName  the userName
     * @param campus the campus
     * @return question optional
     */
    public Campus create(final String userName,
                            final Campus campus) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("campuses")
                .usingColumns("id", "title",
                        "description",
                        "created_by");

        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("title",
                campus.title());
        valueMap.put("description", campus.description());
        valueMap.put("created_by", userName);

        final UUID campusId = UUID.randomUUID();
        valueMap.put("id", campusId);
        insert.execute(valueMap);
        final Optional<Campus> createdCourse =
                read(userName, campusId);

        logger.info("Created Campus {}", campusId);

        return createdCourse.get();
    }


    /**
     * reads from campus.
     *
     * @param id       the id
     * @param userName the userName
     * @return question optional
     */
    public Optional<Campus> read(final String userName, final UUID id) {
        final String query = "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM campuses "
                + "WHERE id = ?";


        try {
            final Campus p = jdbcTemplate
                    .queryForObject(query, this::rowMapper, id);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * update the campus.
     *
     * @param id        the id
     * @param userName  the userName
     * @param campus the campus
     * @return question optional
     */
    public Campus update(final UUID id,
                            final String userName,
                            final Campus campus) {
        logger.debug("Entering Update for Campus {}", id);
        final String query =
                "UPDATE campuses SET title = ?,"
                        + "description = ?, modified_by = ? WHERE id = ?";
        final Integer updatedRows =
                jdbcTemplate.update(query, campus.title(),
                        campus.description(), userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Campus not found");
        }
        return read(userName, id).get();
    }

    /**
     * delete the campus.
     *
     * @param id       the id
     * @param userName the userName
     * @return false
     */
    public Boolean delete(final String userName, final UUID id) {
        String query = "DELETE FROM campuses WHERE ID=?";

        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }


    /**
     * list of campuses.
     *
     * @param userName the userName
     * @return campuses list
     */
    public List<Campus> list(final String userName) {
        String query = "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM campuses";
        return jdbcTemplate.query(query, this::rowMapper);
    }

    /**
     * Cleaning up all campuses.
     *
     * @return no.of campuses deleted
     */
    public Integer deleteAll() {
        final String query = "DELETE FROM campuses";
        return jdbcTemplate.update(query);
    }
}
