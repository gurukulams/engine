package com.techatpark.workout.service;

import com.techatpark.workout.model.Grade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Grade service.
 */
@Service
public class GradeService {

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
    private final Logger logger = LoggerFactory.getLogger(GradeService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;

    /**
     * Instantiates a new Grade service.
     *
     * @param aJdbcClient the jdbc client
     */
    public GradeService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private Grade rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Grade grade = new Grade((UUID) rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDateTime.class),
                rs.getString(INDEX_5),
                rs.getObject(INDEX_6, LocalDateTime.class),
                rs.getString(INDEX_7));

        return grade;
    }

    /**
     * Create grade.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param grade    the grade
     * @return the grade
     */
    public Grade create(final String userName,
                        final Locale locale,
                        final Grade grade) {
        final String insertGradeQuery = """
                INSERT INTO %s(id, title, description, created_by)
                VALUES (?, ?, ?, ?)
                """.formatted(GRADES_TABLE);

        final UUID gradeId = UUID.randomUUID();
        jdbcClient.sql(insertGradeQuery)
                .param(INDEX_1, gradeId)
                .param(INDEX_2, grade.title())
                .param(INDEX_3, grade.description())
                .param(INDEX_4, userName)
                .update();

        if (locale != null) {
            createLocalizedGrade(gradeId, grade, locale);
        }

        return read(userName, null, gradeId).get();
    }

    private int createLocalizedGrade(final UUID gradeId,
                                     final Grade grade,
                                     final Locale locale) {
        final String insertLocalizedGradeQuery = """
                INSERT INTO %s(grade_id, locale, title, description)
                VALUES (?, ?, ?, ?)
                """.formatted(GRADES_LOCALIZED_TABLE);

        return jdbcClient.sql(insertLocalizedGradeQuery)
                .param(INDEX_1, gradeId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, grade.title())
                .param(INDEX_4, grade.description())
                .update();
    }

    /**
     * Read optional.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param id       the id
     * @return the optional
     */
    public Optional<Grade> read(final String userName,
                                final Locale locale,
                                final UUID id) {
        final String selectGradeQuery = locale == null
                ?
                """
                        SELECT id, title, description, created_at, created_by,
                        modified_at, modified_by
                        FROM %s
                        WHERE id = ?
                        """.formatted(GRADES_TABLE)
                :
                """
                        SELECT DISTINCT g.ID,
                            CASE WHEN gl.LOCALE = ?
                                THEN gl.TITLE
                                ELSE g.TITLE
                            END AS TITLE,
                            CASE WHEN gl.LOCALE = ?
                                THEN gl.DESCRIPTION
                                ELSE g.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM %s g
                        LEFT JOIN %s gl ON g.ID = gl.GRADE_ID
                        WHERE g.ID = ?
                            AND (gl.LOCALE IS NULL
                            OR gl.LOCALE = ?
                            OR g.ID NOT IN (
                                SELECT GRADE_ID
                                FROM %s
                                WHERE GRADE_ID = g.ID
                                    AND LOCALE = ?
                            ))
                        """.formatted(GRADES_TABLE, GRADES_LOCALIZED_TABLE,
                        GRADES_LOCALIZED_TABLE);


            return locale == null
                    ?
                    jdbcClient.sql(selectGradeQuery)
                            .param(INDEX_1, id)
                            .query(this::rowMapper)
                            .optional()
                    :
                    jdbcClient.sql(selectGradeQuery)
                            .param(INDEX_1, locale.getLanguage())
                            .param(INDEX_2, locale.getLanguage())
                            .param(INDEX_3, id)
                            .param(INDEX_4, locale.getLanguage())
                            .param(INDEX_5, locale.getLanguage())
                            .query(this::rowMapper)
                            .optional();

    }

    /**
     * Update grade.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param grade    the grade
     * @return the grade
     */
    public Grade update(final UUID id,
                        final String userName,
                        final Locale locale,
                        final Grade grade) {
        logger.debug("Entering update for Grade {}", id);
        final String updateGradeQuery = locale == null
                ?
                """
                        UPDATE %s
                        SET title = ?, description = ?, modified_by = ?
                        WHERE id = ?
                        """.formatted(GRADES_TABLE)
                :
                "UPDATE %s SET modified_by = ? WHERE id = ?"
                        .formatted(GRADES_TABLE);

        Integer updatedRows = locale == null
                ?
                jdbcClient.sql(updateGradeQuery)
                        .param(INDEX_1, grade.title())
                        .param(INDEX_2, grade.description())
                        .param(INDEX_3, userName)
                        .param(INDEX_4, id)
                        .update()
                :
                jdbcClient.sql(updateGradeQuery)
                        .param(INDEX_1, userName)
                        .param(INDEX_2, id)
                        .update();

        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Grade not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql("""
                            UPDATE %s
                            SET title = ?, locale = ?, description = ?
                            WHERE grade_id = ?
                            AND locale = ?
                            """.formatted(GRADES_LOCALIZED_TABLE))
                    .param(INDEX_1, grade.title())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, grade.description())
                    .param(INDEX_4, id)
                    .param(INDEX_5, locale.getLanguage())
                    .update();
            if (updatedRows == 0) {
                createLocalizedGrade(id, grade, locale);
            }
        }

        return read(userName, locale, id).get();
    }

    /**
     * Delete boolean.
     *
     * @param userName the user name
     * @param id       the id
     * @return the boolean
     */
    public Boolean delete(final String userName, final UUID id) {
        final String deleteGradeQuery =
                "DELETE FROM %s WHERE id = ?".formatted(GRADES_TABLE);
        return jdbcClient.sql(deleteGradeQuery)
                .param(INDEX_1, id)
                .update() != 0;
    }

    /**
     * List list.
     *
     * @param userName the user name
     * @param locale   the locale
     * @return the list
     */
    public List<Grade> list(final String userName,
                            final Locale locale) {
        final String listGradeQuery = locale == null
                ?
                """
                        SELECT id, title, description, created_at,
                        created_by, modified_at, modified_by FROM %s
                        """.formatted(GRADES_TABLE)
                :
                """
                        SELECT DISTINCT g.ID,
                            CASE WHEN gl.LOCALE = ?
                                THEN gl.TITLE
                                ELSE g.TITLE
                            END AS TITLE,
                            CASE WHEN gl.LOCALE = ?
                                THEN gl.DESCRIPTION
                                ELSE g.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM %s g
                        LEFT JOIN %s gl ON g.ID = gl.GRADE_ID
                        WHERE gl.LOCALE IS NULL
                            OR gl.LOCALE = ?
                            OR g.ID NOT IN (
                                SELECT GRADE_ID
                                FROM %s
                                WHERE GRADE_ID = g.ID
                                    AND LOCALE = ?
                            )
                        """.formatted(GRADES_TABLE, GRADES_LOCALIZED_TABLE,
                        GRADES_LOCALIZED_TABLE);

        return locale == null
                ?
                jdbcClient.sql(listGradeQuery)
                        .query(this::rowMapper)
                        .list()
                :
                jdbcClient.sql(listGradeQuery)
                        .param(INDEX_1, locale.getLanguage())
                        .param(INDEX_2, locale.getLanguage())
                        .param(INDEX_3, locale.getLanguage())
                        .param(INDEX_4, locale.getLanguage())
                        .query(this::rowMapper)
                        .list();
    }

    /**
     * List grades by board.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param boardId  the board id
     * @return the list
     */
    public List<Grade> list(final String userName,
                            final Locale locale,
                            final UUID boardId) {
        final String listGradeByBoardQuery = locale == null
                ?
                """
                        SELECT id, title, description, created_at, created_by,
                        modified_at, modified_by
                        FROM grades
                        JOIN boards_grades ON grades.id = boards_grades.grade_id
                        WHERE boards_grades.board_id = ?
                        """
                :
                """
                        SELECT DISTINCT g.ID,
                            CASE WHEN gl.LOCALE = ?
                                THEN gl.TITLE
                                ELSE g.TITLE
                            END AS TITLE,
                            CASE WHEN gl.LOCALE = ?
                                THEN gl.DESCRIPTION
                                ELSE g.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM grades g
                        LEFT JOIN GRADES_LOCALIZED gl ON g.ID = gl.GRADE_ID
                        LEFT JOIN boards_grades bg ON g.id = bg.grade_id
                        WHERE bg.board_id = ?
                        """;

        return locale == null
                ?
                jdbcClient.sql(listGradeByBoardQuery)
                        .param(INDEX_1, boardId)
                        .query(this::rowMapper)
                        .list()
                :
                jdbcClient.sql(listGradeByBoardQuery)
                        .param(INDEX_1, locale.getLanguage())
                        .param(INDEX_2, locale.getLanguage())
                        .param(INDEX_3, boardId)
                        .query(this::rowMapper)
                        .list();
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM boards_grades").update();
        jdbcClient.sql("DELETE FROM boards_grades_subjects").update();
        jdbcClient.sql("DELETE FROM boards_grades_subjects_books").update();
        jdbcClient.sql("DELETE FROM grades_localized").update();
        jdbcClient.sql("DELETE FROM grades").update();
    }
}
