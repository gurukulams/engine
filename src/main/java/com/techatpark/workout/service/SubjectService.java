package com.techatpark.workout.service;

import com.techatpark.workout.model.Subject;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The type Subjects service.
 */
@Service
public class SubjectService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(SubjectService.class);

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
     * @param ajdbcTemplate a jdbcTemplate
     * @param adataSource   a dataSource
     */
    public SubjectService(final JdbcTemplate ajdbcTemplate,
                          final DataSource adataSource) {
        this.jdbcTemplate = ajdbcTemplate;
        this.dataSource = adataSource;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Subject rowMapper(final ResultSet rs,
                              final Integer rowNum)
            throws SQLException {
        Subject subject = new Subject((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return subject;
    }

    /**
     * creates new subject.
     *
     * @param userName the userName
     * @param locale   the locale
     * @param subject  the syllabus
     * @return syllabus optional
     */
    public Subject create(final String userName,
                          final Locale locale,
                          final Subject subject) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("subjects")
                .usingColumns("id", "title",
                        "description", "created_by");

        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("title", subject.title());
        valueMap.put("description", subject.description());
        valueMap.put("created_by", userName);

        final UUID subjectId = UUID.randomUUID();
        valueMap.put("id", subjectId);
        insert.execute(valueMap);


        if (locale != null) {
            valueMap.put("subject_id", subjectId);
            valueMap.put("locale", locale.getLanguage());
            createLocalizedSubject(valueMap);
        }
        final Optional<Subject> createdSubjects =
                read(userName, null, subjectId);

        logger.info("Subject Created {}", subjectId);

        return createdSubjects.get();
    }

    /**
     * Create Localized subject.
     *
     * @param valueMap
     * @return noOfSubject
     */
    private int createLocalizedSubject(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("subjects_localized")
                .usingColumns("subject_id", "locale", "title", "description")
                .execute(valueMap);
    }

    /**
     * reads from syllabus.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale   the locale
     * @return question optional
     */
    public Optional<Subject> read(final String userName,
                                  final Locale locale,
                                  final UUID id) {
        final String query = locale == null
                ? "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM subjects "
                + "WHERE id = ?"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.DESCRIPTION "
                + "ELSE b.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "created_by,created_at, modified_at, modified_by "
                + "FROM SUBJECTS b "
                + "LEFT JOIN SUBJECTS_LOCALIZED bl "
                + "ON b.ID = bl.SUBJECT_ID "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT SUBJECT_ID FROM SUBJECTS_LOCALIZED "
                + "WHERE SUBJECT_ID=b.ID AND LOCALE = ?))";

        try {
            final Subject p = locale == null ? jdbcTemplate
                    .queryForObject(query, this::rowMapper, id)
                    : jdbcTemplate
                    .queryForObject(query, this::rowMapper,
                                    locale.getLanguage(),
                                    locale.getLanguage(),
                                    id,
                                    locale.getLanguage(),
                                    locale.getLanguage());
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * update the subjects.
     *
     * @param id       the id
     * @param userName the userName
     * @param locale   the locale
     * @param subject  the subjects
     * @return question optional
     */
    public Subject update(final UUID id,
                          final String userName,
                          final Locale locale,
                          final Subject subject) {
        logger.debug("Entering update for Subject {}", id);
        final String query = locale == null
                ? "UPDATE subjects SET title=?,"
                + "description=?,modified_by=? WHERE id=?"
                : "UPDATE subjects SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, subject.title(),
                subject.description(), userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Subject not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE subjects_localized SET title=?,locale=?,"
                            + "description=? WHERE subject_id=? AND locale=?",
                    subject.title(), locale.getLanguage(),
                    subject.description(), id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("subject_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", subject.title());
                valueMap.put("description", subject.description());
                createLocalizedSubject(valueMap);
            }
        }
        return read(userName, locale, id).get();
    }

    /**
     * delete the subject.
     *
     * @param id       the id
     * @param userName the userName
     * @return question optional
     */
    public Boolean delete(final String userName, final UUID id) {
        final String query = "DELETE FROM subjects WHERE id = ?";
        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }

    /**
     * list the subject.
     *
     * @param userName the userName
     * @param locale   the locale
     * @return question optional
     */
    public List<Subject> list(final String userName,
                              final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM subjects"
                : "SELECT DISTINCT b.ID, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.TITLE "
                + "ELSE b.TITLE "
                + "END AS TITLE, "
                + "CASE WHEN bl.LOCALE = ? "
                + "THEN bl.DESCRIPTION "
                + "ELSE b.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "created_by,created_at, modified_at, modified_by "
                + "FROM SUBJECTS b "
                + "LEFT JOIN SUBJECTS_LOCALIZED bl "
                + "ON b.ID = bl.SUBJECT_ID "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT SUBJECT_ID FROM SUBJECTS_LOCALIZED "
                + "WHERE SUBJECT_ID=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage());

    }


    /**
     * list the subject by grade and board.
     *
     * @param userName the userName
     * @param locale   the locale
     * @param boardId  the grade
     * @param gradeId  the grade
     * @return syllabus optional
     */
    public List<Subject> list(final String userName,
                              final Locale locale,
                              final UUID boardId,
                              final UUID gradeId) {
        final String query = locale == null
                ? "SELECT id,title,description,created_by,"
                + "created_at,modified_at,modified_by FROM subjects "
                + "JOIN boards_grades_subjects ON subjects.id "
                + "= boards_grades_subjects.subject_id "
                + " where boards_grades_subjects.grade_id = ? "
                + "AND "
                + " boards_grades_subjects.board_id = ? "
                : " SELECT DISTINCT s.ID, "
                + "CASE WHEN sl.LOCALE = ? "
                + "THEN sl.TITLE "
                + "ELSE s.TITLE "
                + "END AS TITLE, "
                + "CASE WHEN sl.LOCALE = ? "
                + "THEN sl.DESCRIPTION "
                + "ELSE s.DESCRIPTION "
                + "END AS DESCRIPTION,"
                + "created_by,created_at, modified_at, modified_by "
                + "FROM subjects s "
                + "LEFT JOIN SUBJECTS_LOCALIZED sl "
                + "ON s.id = sl.subject_id "
                + "LEFT JOIN boards_grades_subjects bgs "
                + "ON s.id = bgs.subject_id where bgs.grade_id = ? "
                + "AND bgs.board_id = ?";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper, gradeId, boardId)
                : jdbcTemplate
                .query(query, this::rowMapper,
                                locale.getLanguage(),
                                locale.getLanguage(),
                                gradeId,
                                boardId);
    }


    /**
     * Cleaning up all subjects.
     */
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM boards_grades_subjects");
        jdbcTemplate.update("DELETE FROM boards_grades_subjects_books");
        jdbcTemplate.update("DELETE FROM subjects_localized");
        jdbcTemplate.update("DELETE FROM subjects");

    }
}
