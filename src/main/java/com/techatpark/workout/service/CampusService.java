package com.techatpark.workout.service;

import com.gurukulams.core.model.Campuses;
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
 * The type Campuses service.
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

    private Campuses rowMapper(final ResultSet rs,
                               final Integer rowNum)
            throws SQLException {
        Campuses campuses = new Campuses();
        campuses.setId((UUID) rs.getObject(INDEX_1));
        campuses.setTitle(rs.getString(INDEX_2));
        campuses.setDescription(rs.getString(INDEX_3));
        campuses.setCreatedAt(rs.getObject(INDEX_4, LocalDateTime.class));
        campuses.setCreatedBy(rs.getString(INDEX_5));
        campuses.setModifiedAt(rs.getObject(INDEX_6, LocalDateTime.class));
        campuses.setModifiedBy(rs.getString(INDEX_7));
        return  campuses;
    }

    /**
     * Create campus.
     *
     * @param userName the user name
     * @param campus   the campus
     * @return the campus
     */
    public Campuses create(final String userName,
                         final Campuses campus) {
        final String insertCampusQuery = """
                INSERT INTO %s(id, title, description, created_by)
                VALUES (?, ?, ?, ?)
                """.formatted(CAMPUSES_TABLE);

        final UUID campusId = UUID.randomUUID();
        jdbcClient.sql(insertCampusQuery)
                .param(INDEX_1, campusId)
                .param(INDEX_2, campus.getTitle())
                .param(INDEX_3, campus.getDescription())
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
    public Optional<Campuses> read(final String userName, final UUID id) {
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
    public Campuses update(final UUID id,
                         final String userName,
                         final Campuses campus) {
        logger.debug("Entering Update for Campuses {}", id);
        final String updateCampusQuery = """
                UPDATE %s
                SET title = ?, description = ?, modified_by = ?
                WHERE id = ?
                """.formatted(CAMPUSES_TABLE);

        Integer updatedRows = jdbcClient.sql(updateCampusQuery)
                .param(INDEX_1, campus.getTitle())
                .param(INDEX_2, campus.getDescription())
                .param(INDEX_3, userName)
                .param(INDEX_4, id)
                .update();

        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Campuses not found");
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
    public List<Campuses> list(final String userName) {
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
