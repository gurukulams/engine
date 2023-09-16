package com.techatpark.workout.service;

import com.techatpark.workout.model.Degree;
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
 * The type Degree service.
 */
@Service
public final class DegreeService {

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
     * grades table.
     */
    private static final String GRADES_TABLE = "grades";
    /**
     * grades_localized table.
     */
    private static final String GRADES_LOCALIZED_TABLE = "grades_localized";
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(DegreeService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;


    /**
     * Instantiates a new Degree service.
     *
     * @param aJdbcClient the JDBC client
     */
    public DegreeService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
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
                rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDateTime.class),
                rs.getString(INDEX_5),
                rs.getObject(INDEX_6, LocalDateTime.class),
                rs.getString(INDEX_7));

        return degree;
    }

    /**
     * Inserts data.
     *
     * @param userName the userName
     * @param degree   the degree
     * @return degree optional
     */
    public Degree create(final String userName,
                         final Degree degree) {
        String insertDegreeQuery = """
                INSERT INTO degree (id, title, description, created_by)
                VALUES (?, ?, ?, ?)
                """;

        final UUID degreeId = UUID.randomUUID();
        jdbcClient.sql(insertDegreeQuery)
                .param(INDEX_1, degreeId)
                .param(INDEX_2, degree.title())
                .param(INDEX_3, degree.description())
                .param(INDEX_4, userName)
                .update();

        final Optional<Degree> createdDegree = read(userName, degreeId);

        logger.info("Created Degree {}", degreeId);

        return createdDegree.get();
    }

    /**
     * Reads from degree.
     *
     * @param userName the userName
     * @param id       the id
     * @return degree optional
     */
    public Optional<Degree> read(final String userName, final UUID id) {
        String readDegreeQuery = """
                SELECT id, title, description, created_at,
                created_by, modified_at, modified_by
                FROM degree
                WHERE id = ?
                """;


            return jdbcClient.sql(readDegreeQuery)
                    .param(INDEX_1, id)
                    .query(this::rowMapper)
                    .optional();

    }

    /**
     * Update the degree.
     *
     * @param id       the id
     * @param userName the userName
     * @param degree   the degree
     * @return degree optional
     */
    public Degree update(final UUID id,
                         final String userName,
                         final Degree degree) {
        logger.debug("Entering Update for Degree {}", id);
        String updateDegreeQuery = """
                UPDATE degree
                SET title = ?, description = ?, modified_by = ?
                WHERE id = ?
                """;

        Integer updatedRows = jdbcClient.sql(updateDegreeQuery)
                .param(INDEX_1, degree.title())
                .param(INDEX_2, degree.description())
                .param(INDEX_3, userName)
                .param(INDEX_4, id)
                .update();

        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Degree not found");
        }

        return read(userName, id).get();
    }

    /**
     * Delete the degree.
     *
     * @param userName the userName
     * @param id       the id
     * @return false boolean
     */
    public Boolean delete(final String userName, final UUID id) {
        String deleteDegreeQuery = """
                DELETE FROM degree WHERE ID=?
                """;

        Integer updatedRows = jdbcClient.sql(deleteDegreeQuery)
                .param(INDEX_1, id)
                .update();

        return !(updatedRows == 0);
    }

    /**
     * List of degree.
     *
     * @param userName the userName
     * @return degree list
     */
    public List<Degree> list(final String userName) {
        String listDegreeQuery = """
                SELECT id, title, description, created_at,
                created_by, modified_at, modified_by
                FROM degree
                """;

        return jdbcClient.sql(listDegreeQuery)
                .query(this::rowMapper)
                .list();
    }

    /**
     * Cleaning up all degree.
     *
     * @return no. of degree deleted
     */
    public Integer deleteAll() {
        String deleteAllDegreesQuery = """
                DELETE FROM degree
                """;

        return jdbcClient.sql(deleteAllDegreesQuery)
                .update();
    }
}
