package com.techatpark.workout.service;

import com.techatpark.workout.model.Campus;
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
 * The type Campus service.
 */
@Service
public final class CampusService {

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
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(CampusService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;
    /**
     * campuses table.
     */
    private static final String CAMPUSES_TABLE = "campuses";

    /**
     * Constructor for CampusService.
     *
     * @param aJdbcClient The JDBC client.
     */
    public CampusService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private Campus rowMapper(final ResultSet rs,
                             final Integer rowNum)
            throws SQLException {
        return new Campus(
                (UUID) rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDateTime.class),
                rs.getString(INDEX_5),
                rs.getObject(INDEX_6, LocalDateTime.class),
                rs.getString(INDEX_7)
        );
    }

    /**
     * Create campus.
     *
     * @param userName the user name
     * @param campus   the campus
     * @return the campus
     */
    public Campus create(final String userName,
                         final Campus campus) {
        final String insertCampusQuery = """
                INSERT INTO %s(id, title, description, created_by)
                VALUES (?, ?, ?, ?)
                """.formatted(CAMPUSES_TABLE);

        final UUID campusId = UUID.randomUUID();
        jdbcClient.sql(insertCampusQuery)
                .param(INDEX_1, campusId)
                .param(INDEX_2, campus.title())
                .param(INDEX_3, campus.description())
                .param(INDEX_4, userName)
                .update();

        return read(userName, campusId).get();
    }

    /**
     * Read optional.
     *
     * @param userName the user name
     * @param id       the id
     * @return the optional
     */
    public Optional<Campus> read(final String userName, final UUID id) {
        final String selectCampusQuery = """
                SELECT id, title, description, created_at,
                       created_by, modified_at, modified_by
                FROM %s
                WHERE id = ?
                """.formatted(CAMPUSES_TABLE);


            return jdbcClient.sql(selectCampusQuery)
                    .param(INDEX_1, id)
                    .query(this::rowMapper)
                    .optional();

    }

    /**
     * Update campus.
     *
     * @param id       the id
     * @param userName the user name
     * @param campus   the campus
     * @return the campus
     */
    public Campus update(final UUID id,
                         final String userName,
                         final Campus campus) {
        logger.debug("Entering Update for Campus {}", id);
        final String updateCampusQuery = """
                UPDATE %s
                SET title = ?, description = ?, modified_by = ?
                WHERE id = ?
                """.formatted(CAMPUSES_TABLE);

        Integer updatedRows = jdbcClient.sql(updateCampusQuery)
                .param(INDEX_1, campus.title())
                .param(INDEX_2, campus.description())
                .param(INDEX_3, userName)
                .param(INDEX_4, id)
                .update();

        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Campus not found");
        }

        return read(userName, id).get();
    }

    /**
     * Delete boolean.
     *
     * @param userName the user name
     * @param id       the id
     * @return the boolean
     */
    public Boolean delete(final String userName, final UUID id) {
        final String deleteCampusQuery =
                "DELETE FROM %s WHERE id = ?".formatted(CAMPUSES_TABLE);
        return jdbcClient.sql(deleteCampusQuery)
                .param(INDEX_1, id)
                .update() != 0;
    }

    /**
     * List list.
     *
     * @param userName the user name
     * @return the list
     */
    public List<Campus> list(final String userName) {
        final String listCampusesQuery = """
                SELECT id, title, description, created_at,
                       created_by, modified_at, modified_by
                FROM %s
                """.formatted(CAMPUSES_TABLE);

        return jdbcClient.sql(listCampusesQuery)
                .query(this::rowMapper)
                .list();
    }

    /**
     * Delete all integer.
     *
     * @return the integer
     */
    public Integer deleteAll() {
        final String deleteAllCampusesQuery =
                "DELETE FROM %s".formatted(CAMPUSES_TABLE);
        return jdbcClient.sql(deleteAllCampusesQuery).update();
    }
}
