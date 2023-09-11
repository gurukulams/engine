package com.techatpark.workout.service;

import com.techatpark.workout.model.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

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
     * this is the constructor.
     *
     * @param ajdbcClient jdbcClient
     */
    public BoardService(final JdbcClient ajdbcClient) {
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
        return new Board((UUID)
                rs.getObject(1),
                rs.getString(2),
                rs.getString(3),
                rs.getObject(4, LocalDateTime.class),
                rs.getString(5),
                rs.getObject(6, LocalDateTime.class),
                rs.getString(7));
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
        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("title", board.title());
        valueMap.put("description", board.description());
        valueMap.put("created_by", userName);
        final UUID boardId = UUID.randomUUID();
        valueMap.put("id", boardId);


        String sql =
                "INSERT INTO boards(id, title, description, "
                        + "created_by) values(:id, :title, :description, "
                        + ":created_by)";
        jdbcClient.sql(sql).params(valueMap).update();

        if (locale != null) {
            valueMap.put("board_id", boardId);
            valueMap.put("locale", locale.getLanguage());
            createLocalizedBoard(valueMap);
        }

        final Optional<Board> createdBoard =
                read(userName, locale, boardId);

        logger.info("Board Created {}", boardId);

        return createdBoard.get();
    }

    /**
     * Create Localized Board.
     *
     * @param valueMap
     * @return noOfBoards
     */
    private int createLocalizedBoard(final Map<String, Object> valueMap) {
        String sql =
                "INSERT INTO boards_localized(board_id, locale, title, "
                        + "description) values(:board_id, :locale, :title, "
                        + ":description)";
        return jdbcClient.sql(sql).params(valueMap).update();
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
                ? "SELECT id,title,description,created_at,"
                + "created_by, modified_at, modified_by FROM boards "
                + "WHERE id = ?"
                : """
                SELECT
                    b.id,
                    COALESCE(bl.title, b.title) AS title,
                    COALESCE(bl.description, b.description) AS description,
                    b.created_at,
                    b.created_by,
                    b.modified_at,
                    b.modified_by
                 FROM
                    boards b
                 LEFT JOIN
                    boards_localized bl
                 ON
                    b.id = bl.board_id
                    AND bl.locale = ?
                WHERE
                    b.id = ?
                 """;

        try {
            return locale == null
                    ? jdbcClient.sql(query).param(1, id)
                    .query(Board.class).optional()
                    : jdbcClient.sql(query)
                    .param(1, locale.getLanguage())
                    .param(2, id)
                    .query(Board.class).optional();
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
        List params = locale == null ? List.of(
                board.title(), board.description(), userName, id)
                : List.of(userName, id);
        int updatedRows = jdbcClient.sql(query).params(params).update();
        if (updatedRows == 0) {
            logger.error("Update not found {}", id);
            throw new IllegalArgumentException("Board not found");
        } else if (locale != null) {
            updatedRows = jdbcClient.sql(
                            "UPDATE boards_localized SET title=?,locale=?,"
                                    + "description=? WHERE board_id=? AND "
                                    + "locale=?")
                    .params(List.of(board.title(), locale.getLanguage(),
                            board.description(), id, locale.getLanguage()))
                    .update();
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
        return jdbcClient.sql("DELETE FROM boards WHERE id = ?")
                .param(1, id).update() != 0;
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
                ? "SELECT id,title,description,created_at,"
                + "created_by, modified_at, modified_by FROM boards "
                : """
                 SELECT
                    b.id,
                    COALESCE(bl.title, b.title) AS title,
                    COALESCE(bl.description, b.description) AS description,
                    b.created_at,
                    b.created_by,
                    b.modified_at,
                    b.modified_by
                 FROM
                    boards b
                 LEFT JOIN
                    boards_localized bl
                 ON
                    b.id = bl.board_id
                    AND bl.locale = ?
                    """;
        return locale == null
                ? jdbcClient.sql(query).query(this::rowMapper).list()
                : jdbcClient.sql(query)
                .param(1, locale.getLanguage())
                .query(Board.class).list();

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
        String sql =
                "INSERT INTO boards_grades(board_id, grade_id)"
                        + "values(?, ?)";

        return jdbcClient.sql(sql)
                .param(1, boardId)
                .param(2, gradeId)
                .update() == 1;
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
        String sql =
                "INSERT INTO boards_grades_subjects(board_id, grade_id,"
                        + " subject_id) values(?,?,?)";

        return jdbcClient.sql(sql)
                .param(1, boardId)
                .param(2, gradeId)
                .param(3, subjectId)
                .update() == 1;
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
        String sql =
                "INSERT INTO boards_grades_subjects_books(board_id, grade_id,"
                        + " subject_id, "
                        + "book_id) values(?,?,?,?)";

        return jdbcClient.sql(sql)
                .param(1, boardId)
                .param(2, gradeId)
                .param(3, subjectId)
                .param(4, bookId)
                .update() == 1;
    }

    /**
     * Cleaning up all boards.
     */
    public void deleteAll() {
        jdbcClient.sql("DELETE FROM boards_grades_subjects_books").update();
        jdbcClient.sql("DELETE FROM boards_grades_subjects").update();
        jdbcClient.sql("DELETE FROM boards_grades").update();
        jdbcClient.sql("DELETE FROM boards_localized").update();
        jdbcClient.sql("DELETE FROM boards").update();
    }
}
