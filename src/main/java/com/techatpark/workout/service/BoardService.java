package com.techatpark.workout.service;

import com.techatpark.workout.model.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.JdbcClient;
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


@Service
public class BoardService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(BoardService.class);

    /**
     * this helps to execute sql queries.
     */
    private final JdbcClient jdbcClient;

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
     * @param ajdbcClient jdbcClient
     * @param adataSource   a dataSource
     */
    public BoardService(final JdbcTemplate ajdbcTemplate,
                        final JdbcClient ajdbcClient,
                        final DataSource adataSource) {
        this.jdbcTemplate = ajdbcTemplate;
        this.dataSource = adataSource;
        this.jdbcClient = ajdbcClient;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Board rowMapper(final ResultSet rs,
                            final Integer rowNum)
            throws SQLException {
        Board board = new Board((UUID)
                rs.getObject("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by"));

        return board;
    }

    /**
     * creates new syllabus.
     *
     * @param userName the userName
     * @param board    the syllabus
     * @param locale   the locale
     * @return board optional
     */
    public Board create(final String userName,
                        final Locale locale,
                        final Board board) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("boards")
                .usingColumns("id", "title",
                        "description", "created_by");

        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("title", board.title());
        valueMap.put("description", board.description());
        valueMap.put("created_by", userName);

        final UUID boardId = UUID.randomUUID();
        valueMap.put("id", boardId);
        insert.execute(valueMap);


        if (locale != null) {
            valueMap.put("board_id", boardId);
            valueMap.put("locale", locale.getLanguage());
            createLocalizedBoard(valueMap);
        }

        final Optional<Board> createdBoard =
                read(userName, locale, boardId);

        logger.info("Syllabus Created {}", boardId);

        return createdBoard.get();
    }

    /**
     * Create Localized Board.
     *
     * @param valueMap
     * @return noOfBoards
     */
    private int createLocalizedBoard(final Map<String, Object> valueMap) {
        return new SimpleJdbcInsert(dataSource)
                .withTableName("boards_localized")
                .usingColumns("board_id", "locale", "title", "description")
                .execute(valueMap);
    }

    /**
     * reads from syllabus.
     *
     * @param id       the id
     * @param locale   the locale
     * @param userName the userName
     * @return board optional
     */
    public Optional<Board> read(final String userName,
                                final Locale locale, final UUID id) {

        final String query = locale == null
                ? "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM boards "
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
                + "FROM BOARDS b "
                + "LEFT JOIN BOARDS_LOCALIZED bl "
                + "ON b.ID = bl.BOARD_ID "
                + "WHERE b.ID = ? "
                + "AND (bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT BOARD_ID FROM BOARDS_LOCALIZED "
                + "WHERE BOARD_ID=b.ID AND LOCALE = ?))";

        try {
            final Board p = locale == null ? jdbcTemplate
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
     * update the board.
     *
     * @param id       the id
     * @param userName the userName
     * @param board    the board
     * @param locale   the locale
     * @return board optional
     */
    public Board update(final UUID id,
                        final String userName,
                        final Locale locale,
                        final Board board) {
        logger.debug("Entering update for Board {}", id);
        final String query = locale == null
                ? "UPDATE boards SET title=?,"
                + "description=?,modified_by=? WHERE id=?"
                : "UPDATE boards SET modified_by=? WHERE id=?";
        Integer updatedRows = locale == null
                ? jdbcTemplate.update(query, board.title(),
                board.description(), userName, id)
                : jdbcTemplate.update(query, userName, id);
        if (updatedRows == 0) {
            logger.error("Update not found", id);
            throw new IllegalArgumentException("Board not found");
        } else if (locale != null) {
            updatedRows = jdbcTemplate.update(
                    "UPDATE boards_localized SET title=?,locale=?,"
                            + "description=? WHERE board_id=? AND locale=?",
                    board.title(), locale.getLanguage(),
                    board.description(), id, locale.getLanguage());
            if (updatedRows == 0) {
                final Map<String, Object> valueMap = new HashMap<>(4);
                valueMap.put("board_id", id);
                valueMap.put("locale", locale.getLanguage());
                valueMap.put("title", board.title());
                valueMap.put("description", board.description());
                createLocalizedBoard(valueMap);
            }
        }
        return read(userName, locale, id).get();
    }

    /**
     * delete the board.
     *
     * @param id       the id
     * @param userName the userName
     * @return board optional
     */
    public Boolean delete(final String userName, final UUID id) {
        final String query = "DELETE FROM boards WHERE id = ?";
        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }

    /**
     * list the board.
     *
     * @param userName the userName
     * @param locale   the locale
     * @return board optional
     */
    public List<Board> list(final String userName,
                            final Locale locale) {
        final String query = locale == null
                ? "SELECT id,title,description,created_by,"
                + "created_at, modified_at, modified_by FROM boards"
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
                + "FROM BOARDS b "
                + "LEFT JOIN BOARDS_LOCALIZED bl "
                + "ON b.ID = bl.BOARD_ID "
                + "WHERE bl.LOCALE IS NULL "
                + "OR bl.LOCALE = ? OR "
                + "b.ID NOT IN "
                + "(SELECT BOARD_ID FROM BOARDS_LOCALIZED "
                + "WHERE BOARD_ID=b.ID AND LOCALE = ?)";
        return locale == null
                ? jdbcTemplate.query(query, this::rowMapper)
                : jdbcTemplate
                .query(query, this::rowMapper,
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage(),
                                locale.getLanguage()
                        );

    }

    /**
     * Adds grade to board.
     *
     * @param userName the userName
     * @param boardId  the boardId
     * @param gradeId  the gradeId
     * @return grade optional
     */
    public boolean attachGrade(final String userName, final UUID boardId,
                               final UUID gradeId) {
        // Insert to boards_grades
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("boards_grades")
                .usingColumns("board_id", "grade_id");

        // Fill the values
        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("board_id", boardId);
        valueMap.put("grade_id", gradeId);

        int noOfRowsInserted = insert.execute(valueMap);

        return noOfRowsInserted == 1;
    }

    /**
     * Adds subject to grade and board.
     *
     * @param userName  the userName
     * @param boardId   the gradeId
     * @param gradeId   the gradeId
     * @param subjectId the syllabusId
     * @return grade optional
     */
    public boolean attachSubject(final String userName,
                                 final UUID boardId,
                                 final UUID gradeId,
                                 final UUID subjectId) {
        // Insert to boards_grades
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("boards_grades_subjects")
                .usingColumns("board_id", "grade_id", "subject_id");

        // Fill the values
        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("board_id", boardId);
        valueMap.put("grade_id", gradeId);
        valueMap.put("subject_id", subjectId);

        int noOfRowsInserted = insert.execute(valueMap);

        return noOfRowsInserted == 1;
    }

    /**
     * Adds book to grade, board and subject.
     *
     * @param userName  the userName
     * @param boardId   the gradeId
     * @param gradeId   the gradeId
     * @param subjectId the syllabusId
     * @param bookId    the bookId
     * @return grade optional
     */
    public boolean attachBook(final String userName,
                              final UUID boardId,
                              final UUID gradeId,
                              final UUID subjectId,
                              final UUID bookId) {
        // Insert to boards_grades
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("boards_grades_subjects_books")
                .usingColumns("board_id", "grade_id", "subject_id", "book_id");

        // Fill the values
        final Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("board_id", boardId);
        valueMap.put("grade_id", gradeId);
        valueMap.put("subject_id", subjectId);
        valueMap.put("book_id", bookId);

        int noOfRowsInserted = insert.execute(valueMap);

        return noOfRowsInserted == 1;
    }

    /**
     * Cleaning up all boards.
     */
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM boards_grades_subjects_books");
        jdbcTemplate.update("DELETE FROM boards_grades_subjects");
        jdbcTemplate.update("DELETE FROM boards_grades");
        jdbcTemplate.update("DELETE FROM boards_localized");
        jdbcTemplate.update("DELETE FROM boards");
    }
}
