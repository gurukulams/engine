package com.techatpark.workout.service;

import com.techatpark.workout.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
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
 * The type Subjects service.
 */
@Service
public class SubjectService {

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
     * subjects table.
     */
    private static final String SUBJECTS_TABLE = "subjects";
    /**
     * subjects_localized table.
     */
    private static final String SUBJECTS_LOCALIZED_TABLE = "subjects_localized";
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(SubjectService.class);
    /**
     * JdbcClient.
     */
    private final JdbcClient jdbcClient;

    /**
     * Instantiates a new Subject service.
     *
     * @param aJdbcClient the jdbc client
     */
    public SubjectService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private Subject rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        Subject subject = new Subject((UUID) rs.getObject(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDateTime.class),
                rs.getString(INDEX_5),
                rs.getObject(INDEX_6, LocalDateTime.class),
                rs.getString(INDEX_7));

        return subject;
    }

    /**
     * Create subject.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param subject  the subject
     * @return the subject
     */
    public Subject create(final String userName,
                          final Locale locale,
                          final Subject subject) {
        final String insertSubjectQuery = """
                INSERT INTO %s(id, title, description, created_by)
                VALUES (?, ?, ?, ?)
                """.formatted(SUBJECTS_TABLE);

        final UUID subjectId = UUID.randomUUID();
        jdbcClient.sql(insertSubjectQuery)
                .param(INDEX_1, subjectId)
                .param(INDEX_2, subject.title())
                .param(INDEX_3, subject.description())
                .param(INDEX_4, userName)
                .update();

        if (locale != null) {
            createLocalizedSubject(subjectId, subject, locale);
        }

        return read(userName, null, subjectId).get();
    }

    private int createLocalizedSubject(final UUID subjectId,
                                       final Subject subject,
                                       final Locale locale) {
        final String insertLocalizedSubjectQuery = """
                INSERT INTO %s(subject_id, locale, title, description)
                VALUES (?, ?, ?, ?)
                """.formatted(SUBJECTS_LOCALIZED_TABLE);

        return jdbcClient.sql(insertLocalizedSubjectQuery)
                .param(INDEX_1, subjectId)
                .param(INDEX_2, locale.getLanguage())
                .param(INDEX_3, subject.title())
                .param(INDEX_4, subject.description())
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
    public Optional<Subject> read(final String userName,
                                  final Locale locale,
                                  final UUID id) {
        final String selectSubjectQuery = locale == null
                ?
                """
                        SELECT id, title, description, created_at, created_by,
                        modified_at, modified_by
                        FROM %s
                        WHERE id = ?
                        """.formatted(SUBJECTS_TABLE)
                :
                """
                        SELECT DISTINCT s.ID,
                            CASE WHEN sl.LOCALE = ?
                                THEN sl.TITLE
                                ELSE s.TITLE
                            END AS TITLE,
                            CASE WHEN sl.LOCALE = ?
                                THEN sl.DESCRIPTION
                                ELSE s.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM %s s
                        LEFT JOIN %s sl ON s.ID = sl.SUBJECT_ID
                        WHERE s.ID = ?
                            AND (sl.LOCALE IS NULL
                            OR sl.LOCALE = ?
                            OR s.ID NOT IN (
                                SELECT SUBJECT_ID
                                FROM %s
                                WHERE SUBJECT_ID = s.ID
                                    AND LOCALE = ?
                            ))
                        """.formatted(SUBJECTS_TABLE, SUBJECTS_LOCALIZED_TABLE,
                        SUBJECTS_LOCALIZED_TABLE);


            return locale == null
                    ?
                    jdbcClient.sql(selectSubjectQuery)
                            .param(INDEX_1, id)
                            .query(this::rowMapper)
                            .optional()
                    :
                    jdbcClient.sql(selectSubjectQuery)
                            .param(INDEX_1, locale.getLanguage())
                            .param(INDEX_2, locale.getLanguage())
                            .param(INDEX_3, id)
                            .param(INDEX_4, locale.getLanguage())
                            .param(INDEX_5, locale.getLanguage())
                            .query(this::rowMapper)
                            .optional();

    }

    /**
     * Update subject.
     *
     * @param id       the id
     * @param userName the user name
     * @param locale   the locale
     * @param subject  the subject
     * @return the subject
     */
    public Subject update(final UUID id,
                          final String userName,
                          final Locale locale,
                          final Subject subject) {
        logger.debug("Entering update for Subject {}", id);
        final String updateSubjectQuery = locale == null
                ?
                """
                        UPDATE %s
                        SET title = ?, description = ?, modified_by = ?
                        WHERE id = ?
                        """.formatted(SUBJECTS_TABLE)
                :
                "UPDATE %s SET modified_by = ? WHERE id = ?"
                        .formatted(SUBJECTS_TABLE);

        Integer updatedRows = locale == null
                ?
                jdbcClient.sql(updateSubjectQuery)
                        .param(INDEX_1, subject.title())
                        .param(INDEX_2, subject.description())
                        .param(INDEX_3, userName)
                        .param(INDEX_4, id)
                        .update()
                :
                jdbcClient.sql(updateSubjectQuery)
                        .param(INDEX_1, userName)
                        .param(INDEX_2, id)
                        .update();

        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Subject not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql("""
                            UPDATE %s
                            SET title = ?, locale = ?, description = ?
                            WHERE subject_id = ?
                            AND locale = ?
                            """.formatted(SUBJECTS_LOCALIZED_TABLE))
                    .param(INDEX_1, subject.title())
                    .param(INDEX_2, locale.getLanguage())
                    .param(INDEX_3, subject.description())
                    .param(INDEX_4, id)
                    .param(INDEX_5, locale.getLanguage())
                    .update();
            if (updatedRows == 0) {
                createLocalizedSubject(id, subject, locale);
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
        final String deleteSubjectQuery =
                "DELETE FROM %s WHERE id = ?".formatted(SUBJECTS_TABLE);
        return jdbcClient.sql(deleteSubjectQuery)
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
    public List<Subject> list(final String userName,
                              final Locale locale) {
        final String listSubjectQuery = locale == null
                ?
                """
                        SELECT id, title, description, created_at,
                        created_by, modified_at, modified_by FROM %s
                        """.formatted(SUBJECTS_TABLE)
                :
                """
                        SELECT DISTINCT s.ID,
                            CASE WHEN sl.LOCALE = ?
                                THEN sl.TITLE
                                ELSE s.TITLE
                            END AS TITLE,
                            CASE WHEN sl.LOCALE = ?
                                THEN sl.DESCRIPTION
                                ELSE s.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM %s s
                        LEFT JOIN %s sl ON s.ID = sl.SUBJECT_ID
                        WHERE sl.LOCALE IS NULL
                            OR sl.LOCALE = ?
                            OR s.ID NOT IN (
                                SELECT SUBJECT_ID
                                FROM %s
                                WHERE SUBJECT_ID = s.ID
                                    AND LOCALE = ?
                            )
                        """.formatted(SUBJECTS_TABLE, SUBJECTS_LOCALIZED_TABLE,
                        SUBJECTS_LOCALIZED_TABLE);

        return locale == null
                ?
                jdbcClient.sql(listSubjectQuery)
                        .query(this::rowMapper)
                        .list()
                :
                jdbcClient.sql(listSubjectQuery)
                        .param(INDEX_1, locale.getLanguage())
                        .param(INDEX_2, locale.getLanguage())
                        .param(INDEX_3, locale.getLanguage())
                        .param(INDEX_4, locale.getLanguage())
                        .query(this::rowMapper)
                        .list();
    }

    /**
     * List subjects by grade and board.
     *
     * @param userName the user name
     * @param locale   the locale
     * @param boardId  the board id
     * @param gradeId  the grade id
     * @return the list
     */
    public List<Subject> list(final String userName,
                              final Locale locale,
                              final UUID boardId,
                              final UUID gradeId) {
        final String listSubjectByGradeQuery = locale == null
                ?
                """
                        SELECT id, title, description, created_at,
                        created_by, modified_at, modified_by
                        FROM %s
                        JOIN boards_grades_subjects ON %s.id =
                        boards_grades_subjects.subject_id
                        WHERE boards_grades_subjects.grade_id = ?
                        AND boards_grades_subjects.board_id = ?
                        """.formatted(SUBJECTS_TABLE, SUBJECTS_TABLE)
                :
                """
                        SELECT DISTINCT s.ID,
                            CASE WHEN sl.LOCALE = ?
                                THEN sl.TITLE
                                ELSE s.TITLE
                            END AS TITLE,
                            CASE WHEN sl.LOCALE = ?
                                THEN sl.DESCRIPTION
                                ELSE s.DESCRIPTION
                            END AS DESCRIPTION,
                            created_at, created_by, modified_at, modified_by
                        FROM %s s
                        LEFT JOIN %s sl ON s.ID = sl.SUBJECT_ID
                        LEFT JOIN boards_grades_subjects bgs ON s.id =
                        bgs.subject_id
                        WHERE bgs.grade_id = ?
                        AND bgs.board_id = ?
                        """.formatted(SUBJECTS_TABLE, SUBJECTS_LOCALIZED_TABLE,
                        SUBJECTS_LOCALIZED_TABLE);

        return locale == null
                ?
                jdbcClient.sql(listSubjectByGradeQuery)
                        .param(INDEX_1, gradeId)
                        .param(INDEX_2, boardId)
                        .query(this::rowMapper)
                        .list()
                :
                jdbcClient.sql(listSubjectByGradeQuery)
                        .param(INDEX_1, locale.getLanguage())
                        .param(INDEX_2, locale.getLanguage())
                        .param(INDEX_3, gradeId)
                        .param(INDEX_4, boardId)
                        .query(this::rowMapper)
                        .list();
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM boards_grades_subjects").update();
        jdbcClient.sql("DELETE FROM boards_grades_subjects_books").update();
        jdbcClient.sql("DELETE FROM %s".formatted(SUBJECTS_LOCALIZED_TABLE))
                .update();
        jdbcClient.sql("DELETE FROM %s".formatted(SUBJECTS_TABLE)).update();
    }
}
